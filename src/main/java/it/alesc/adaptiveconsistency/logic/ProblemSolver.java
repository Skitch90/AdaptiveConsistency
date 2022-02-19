package it.alesc.adaptiveconsistency.logic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import it.alesc.adaptiveconsistency.logic.csp.*;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class implements the adaptive consistency algorithm.
 *
 * @author Alessandro Schio
 * @version 8.0 09 Jan 2014
 *
 */
@Slf4j
@UtilityClass
public class ProblemSolver {
	private static final String START_METHOD_LOG_FORMAT = "Start method {}";

	public static CSPResolutionTracker solveProblem(StartInformation startInformation) {
		if (startInformation.toCSP().notSatisfiable()) {
			log.info("{} - CSP (variables={} constraints={}) not satisfiable",
					"solve", startInformation.variables(), startInformation.constraints());
			return new CSPResolutionTracker(startInformation, false);
		}

		return Lists.reverse(startInformation.variableOrder()).stream()
				.reduce(new CSPResolutionTracker(startInformation, true),
						ProblemSolver::nextIteration,
						(tuple2, tuple22) -> tuple22)
				.finish(tracker -> computeSolution(tracker, startInformation.variableOrder()));
	}

	private static CSPResolutionTracker nextIteration(CSPResolutionTracker cspResolutionTracker,
													  String variableName) {
		final String methodName = "nextIteration";
		final int iterationNumber = cspResolutionTracker.lastStepIndex() + 1;
		if (!cspResolutionTracker.hasSolution()) {
			return cspResolutionTracker;
		}

		log.debug("{} - Start iteration #{} variable: {}", methodName, iterationNumber, variableName);
		final Set<Variable> variables = cspResolutionTracker.lastStepVariables();
		final Set<Constraint> constraints = cspResolutionTracker.lastStepConstraints();
		final Optional<Variable> variable = Utils.getVariableFromName(variableName, variables);
		if (variable.isEmpty()) {
			return cspResolutionTracker;
		}

		final List<Variable> parents = getParents(variable.get(), variables, cspResolutionTracker);
		log.debug("{} - iteration #{} - parents: {}", methodName, iterationNumber, parents);
		Constraint newConstraint = consistency(variable.get(), parents, constraints);
		log.debug("{} - iteration #{} - consistency constraint: {}",
				methodName, iterationNumber, newConstraint);
		var consistentCSP = updateCSP(new CSP(variables, constraints), newConstraint);
		log.debug("{} - iteration #{} - updatedCSP: {}", methodName, iterationNumber, consistentCSP);
		final boolean notSatisfiable = consistentCSP.notSatisfiable(newConstraint.getVariables());
		if (notSatisfiable) {
			log.info("{} - iteration #{} - updatedCSP not satisfiable",	methodName, iterationNumber);
		}
		var step = new CSPResolutionStep(iterationNumber, variableName, consistentCSP);
		return cspResolutionTracker.addStep(step, !notSatisfiable);
	}

	private List<Variable> getParents(final Variable variable,
									  final Set<Variable> variables,
									  final CSPResolutionTracker cspResolutionTracker) {
		return cspResolutionTracker.variablesOrder().stream()
				.takeWhile(variableName -> !StringUtils.equals(variableName, variable.getName()))
				.filter(v -> existConstraintBetween(List.of(v, variable.getName()), cspResolutionTracker.lastStepConstraints()))
				.map(name -> Utils.getVariableFromName(name, variables))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
	}

	private Constraint consistency(final Variable variable,
			final List<Variable> parents, final Set<Constraint> constraints) {
		List<Constraint> applicableConstr = getApplicableConstraints(
				constraints, variable.getName(), getNamesFromVariables(parents));
		List<Variable> allVar = CollectionUtils.listOf(variable, parents);
		List<List<String>> allTuples = getAllTuples(allVar);
		List<List<String>> compTuples = filterTuples(allTuples,
				getNamesFromVariables(allVar), applicableConstr);
		final Map<Integer, Integer> positions = IntStream.range(1, allVar.size()).boxed()
				.collect(Collectors.toMap(i -> i - 1, Function.identity()));
		Set<List<String>> projTuples = compTuples.stream()
				.map(tuple -> projectTuple(tuple, positions))
				.collect(Collectors.toSet());

		return new Constraint(getNamesFromVariables(parents), projTuples);
	}

	/*
	 * Returns a new CSP obtained by the specified CSP but modified according to
	 * the specified constraint. The modification can be done in three different
	 * ways: 1) the constraint involves one variable, so I used the tuples of
	 * the constraint to modify the domain of that variable. 2) the constraint
	 * involves more than one variable and the list of variables is a
	 * permutation of a list of an existing one, so I used the tuples of the
	 * constraint to modify the tuples of the existing one. 3) the constraint
	 * involves more than one variable and the list is not a permutation of the
	 * list of an existing one, so I add the constraint to the set of
	 * constraints.
	 */
	private CSP updateCSP(
			final CSP consistentCSP,
			final Constraint constraint) {
		if (constraint.getVariables().isEmpty()) {
			return consistentCSP;
		}
		if (constraint.getVariables().size() == 1) {
			return computeCSPSingleVariable(consistentCSP, constraint);
		}
		return computeCSPMultipleVariables(consistentCSP, constraint);
	}

	private CSP computeCSPMultipleVariables(CSP consistentCSP, Constraint constraint) {
		// the constraint involves more than one variable, so I use it to
		// update the constraint of the CSP with the same variables or, if
		// it does not exist I add it to CSP's constraints
		Set<Constraint> cspConstraints = consistentCSP.constraints();
		var constraintGroups = cspConstraints.stream().collect(Collectors.groupingBy(
				cspConstraint -> CollectionUtils.isPermutation(constraint.getVariables(), cspConstraint.getVariables())));

		final List<Constraint> permutedConstraints = constraintGroups.getOrDefault(true, List.of());
		if (permutedConstraints.isEmpty()) {
			var newConstraints = CollectionUtils.setOf(cspConstraints, constraint);
			return new CSP(consistentCSP.variables(), newConstraints);
		}

		final Stream<Constraint> constraintStream = constraintGroups.getOrDefault(false, List.of()).stream();
		final Stream<Constraint> permutedConstraintsStream = permutedConstraints.stream()
				.map(cspConstraint -> new Tuple2<>(cspConstraint, permuteConstraint(constraint, cspConstraint.getVariables())))
				.filter(t -> t._1.getVariables().equals(t._2.getVariables()))
				.map(t -> new Constraint(t._1.getVariables(), Sets.intersection(t._1.getCompTuples(), t._2.getCompTuples())));
		var newConstraints =  Stream.concat(permutedConstraintsStream, constraintStream).collect(Collectors.toSet());
		return new CSP(consistentCSP.variables(), newConstraints);
	}

	private CSP computeCSPSingleVariable(CSP consistentCSP, Constraint constraint) {
		String variableName = constraint.getVariables().get(0);
		var domain = constraint.getCompTuples().stream().map(list -> list.get(0)).collect(Collectors.toSet());
		final Set<Variable> newVariables = consistentCSP.variables().stream().map(variable -> {
			if (!variable.getName().equals(variableName)) {
				return variable;
			}
			return new Variable(variableName, Sets.intersection(domain, variable.getDomain()));
		}).collect(Collectors.toSet());
		return new CSP(newVariables, consistentCSP.constraints());
	}

	private static CSPResolutionTracker computeSolution(CSPResolutionTracker tracker, List<String> variableOrder) {
		final String methodName = "getSolution";
		log.info(START_METHOD_LOG_FORMAT, methodName);
		if (!tracker.hasSolution()) {
			log.info("{} - the problem has no solution", methodName);
			return tracker;
		}

		final Set<Variable> variables = tracker.lastStepVariables();
		final Map<String, String> solution = variableOrder.stream()
				.peek(variableName -> log.debug("{} - processing variable {}", methodName, variableName))
				.map((String name) -> Utils.getVariableFromName(name, variables))
				.filter(Optional::isPresent).map(Optional::get)
				.reduce(Maps.newTreeMap(),
						(solution1, variable) -> getSolutionForVariable(solution1, variable, tracker.lastStepConstraints()),
						(v1, v2) -> v2);
		log.info("End method {} - result: {}", methodName, solution);
		return tracker.addSolution(solution);
	}

	private TreeMap<String, String> getSolutionForVariable(TreeMap<String, String> solution,
														   Variable variable,
														   Set<Constraint> constraints) {
		final String methodName = "getSolutionForVariable";
		final List<String> solutionVariables = Lists.newArrayList(solution.keySet());
		final List<String> allVariables = CollectionUtils.listOf(solutionVariables, variable.getName());
		List<Constraint> appConstraints = getApplicableConstraints(	constraints, variable.getName(), solutionVariables);
		log.debug("{} - variable {} - applicable constraints: {}",
				methodName, variable.getName(), appConstraints);
		final Optional<String> solutionValue = variable.getDomain().stream()
				.peek(value -> log.debug("{} - variable {} - processing value: {}",
						methodName, variable.getName(), value))
				.filter(value -> isAcceptable(CollectionUtils.listOf(solution.values(), value),
						allVariables, appConstraints))
				.findFirst();
		var updatedSolution = Maps.newTreeMap(solution);
		solutionValue.ifPresent(value -> {
			log.debug("{} - variable {} - value {} is acceptable", methodName, variable.getName(), value);
			updatedSolution.put(variable.getName(), value);
		});
		log.debug("{} - variable {} - updated solution: {}", methodName, variable.getName(), solution);
		return updatedSolution;
	}


	private boolean existConstraintBetween(final List<String> names, Set<Constraint> constraints) {
		return constraints.stream().anyMatch(constraint -> constraint.getVariables().containsAll(names));
	}

	private List<Constraint> getApplicableConstraints(
			final Set<Constraint> constraints, final String variableName,
			final List<String> parentsNames) {
		final List<String> namesList = CollectionUtils.listOf(variableName, parentsNames);
		return constraints.stream()
				.filter(constraint -> constraint.getVariables().contains(variableName)
					&& namesList.containsAll(constraint.getVariables()))
				.toList();
	}

	private List<List<String>> getAllTuples(final List<Variable> variables) {
		if (variables.isEmpty()) {
			return List.of(List.of());
		}

		Set<String> firstVarDomain = variables.get(0).getDomain();
		List<Variable> varTail = variables.subList(1, variables.size());

		List<List<String>> subProbTuples = getAllTuples(varTail);
		log.debug("all tuples for {}: {}", varTail, subProbTuples);
		return CollectionUtils.cartesianProduct(List.copyOf(firstVarDomain), subProbTuples).stream()
				.map(t -> CollectionUtils.listOf(t._1, t._2)).toList();
	}

	private List<List<String>> filterTuples(final List<List<String>> allTuples,
			final List<String> variableNames, final List<Constraint> constraints) {
		final String methodName = "filterTuples";
		if (constraints.isEmpty() || allTuples.isEmpty()) {
			return allTuples;
		}

		Constraint firstConstraint = constraints.get(0);
		List<Constraint> constraintsTail = constraints.subList(1, constraints.size());
		List<List<String>> filteredTail = filterTuples(allTuples, variableNames, constraintsTail);
		log.debug("{} - variablesNamesPositions for list {} and {}",
				methodName, variableNames, firstConstraint.getVariables());
		Map<Integer, Integer> varNamePos = variablesNamesPositions(variableNames, firstConstraint.getVariables());
		log.debug("{} - variablesNamesPositions: {}", methodName, varNamePos);
		return filteredTail.stream().filter(tuple -> isAcceptable(tuple, varNamePos, firstConstraint)).toList();
	}

	private boolean isAcceptable(final List<String> tuple,
								 final List<String> variableNames, final List<Constraint> constraints) {
		return constraints.stream()
				.map(constraint -> Tuple.of(constraint, constraintVariablesPosition(variableNames, constraint)))
				.allMatch(t -> isAcceptable(tuple, t._2, t._1));
	}

	private Map<Integer, Integer> constraintVariablesPosition(List<String> variableNames, Constraint constraint) {
		log.debug("{} - variablesNamesPositions for list {} and {}",
				"constraintVariablesPosition", variableNames, constraint.getVariables());
		Map<Integer, Integer> varNamePos = variablesNamesPositions(variableNames, constraint.getVariables());
		log.debug("{} - variablesNamesPositions: {}", "constraintVariablesPosition", varNamePos);
		return varNamePos;
	}

	private boolean isAcceptable(final List<String> tuple,
								 Map<Integer, Integer> varNamePos, final Constraint constraint) {
		final String methodName = "isAcceptable";
		List<String> projTuple = projectTuple(tuple, varNamePos);
		log.debug("{} - project tuple {} -> {}", methodName, tuple, projTuple);
		return constraint.getCompTuples().stream().anyMatch(projTuple::equals);
	}

	private Map<Integer, Integer> variablesNamesPositions(final List<String> variableNames,
														  final List<String> constraintNames) {
		return IntStream.range(0, constraintNames.size()).boxed()
				.collect(Collectors.toMap(Function.identity(), j -> variableNames.indexOf(constraintNames.get(j))));
	}

	private static List<String> projectTuple(List<String> tuple, Map<Integer, Integer> positions) {
		return positions.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.map(entry -> tuple.get(entry.getValue())).toList();
	}

	private Constraint permuteConstraint(final Constraint constraint,
			final List<String> variablesNames) {
		if (variablesNames.equals(constraint.getVariables())) {
			return constraint;
		}

		Map<Integer, Integer> permutationMap = computePermutationMap(
				constraint.getVariables(), variablesNames);
		Set<List<String>> permTuples = constraint.getCompTuples().stream()
				.map(tuple -> permuteTuple(tuple, permutationMap)).collect(Collectors.toSet());

		return new Constraint(variablesNames, permTuples);
	}

	private List<String> permuteTuple(List<String> tuple, Map<Integer, Integer> permutationMap) {
		return permutationMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.map(entry -> tuple.get(entry.getKey())).toList();
	}

	private <T> Map<Integer, Integer> computePermutationMap(
			final List<T> sourceList, final List<T> objectList) {
		 return IntStream.range(0, sourceList.size())
				.filter(i -> objectList.contains(sourceList.get(i))).boxed()
				.collect(Collectors.toMap(Function.identity(), i -> objectList.indexOf(sourceList.get(i))));
	}

	private List<String> getNamesFromVariables(final List<Variable> variables) {
		return variables.stream().map(Variable::getName).toList();
	}
}
