package it.alesc.adaptiveconsistency.logic;

import it.alesc.adaptiveconsistency.gui.ResultFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import it.alesc.adaptiveconsistency.logic.csp.Constraint;
import it.alesc.adaptiveconsistency.logic.csp.Variable;
import it.alesc.adaptiveconsistency.logic.exceptions.NotSatisfiableException;

import org.javatuples.Pair;
import org.javatuples.Triplet;

/**
 * This class implements the adaptive consistency algorithm.
 * 
 * @author Alessandro Schio
 * @version 8.0 09 Jan 2014
 * 
 */
public class ProblemSolver {
	/**
	 * the list of variables of the CSP.
	 */
	private Set<Variable> variables;
	/**
	 * the list of constraints of the CSP.
	 */
	private Set<Constraint> constraints;
	/**
	 * the variables order for the adaptive consistency.
	 */
	private List<String> ordLine;
	/**
	 * The frame in which the progression of the computation is shown
	 */
	private ResultFrame resultFrame;

	/**
	 * The class constructor that creates a new solver with the specified list
	 * of variables, list of constraints and the variables order.
	 * 
	 * @param variables
	 *            the list of variables of the CSP
	 * @param constraints
	 *            the list of constraints of the CSP
	 * @param ordLine
	 *            the variables order
	 */
	public ProblemSolver(final Set<Variable> variables,
			final Set<Constraint> constraints, final List<String> ordLine) {
		this.variables = variables;
		this.constraints = constraints;
		this.ordLine = ordLine;
		this.resultFrame = null;
	}

	/**
	 * The class constructor that creates a new solver with the specified list
	 * of variables, list of constraints, the variables order and the frame to
	 * show computation progression.
	 * 
	 * @param variables
	 *            the list of variables of the CSP
	 * @param constraints
	 *            the list of constraints of the CSP
	 * @param ordLine
	 *            the variables order
	 * @param resultFrame
	 *            the frame where progression of computation is shown or
	 *            <code>null</code> if no progression must be shown
	 */
	public ProblemSolver(final Set<Variable> variables,
			final Set<Constraint> constraints, final List<String> ordLine,
			final ResultFrame resultFrame) {
		this.variables = variables;
		this.constraints = constraints;
		this.ordLine = ordLine;
		this.resultFrame = resultFrame;
	}

	/**
	 * The class constructor that creates a new solver with the specified tuple
	 * that contains the list of variables, the list of constraints and the
	 * variables order.
	 * 
	 * @param data
	 *            the tuple containing the list of variables, the list of
	 *            constraints and the variables order
	 */
	public ProblemSolver(
			final Triplet<Set<Variable>, Set<Constraint>, List<String>> data) {
		this.variables = data.getValue0();
		this.constraints = data.getValue1();
		this.ordLine = data.getValue2();
		this.resultFrame = null;
	}

	/**
	 * The class constructor that creates a new solver with the specified tuple
	 * that contains the list of variables, the list of constraints, the
	 * variables order and the frame to show computation progression.
	 * 
	 * @param data
	 *            the tuple containing the list of variables, the list of
	 *            constraints and the variables order
	 * @param resultFrame
	 *            the frame where progression of computation is shown or
	 *            <code>null</code> if no progression must be shown
	 */
	public ProblemSolver(
			final Triplet<Set<Variable>, Set<Constraint>, List<String>> data,
			final ResultFrame resultFrame) {
		this.variables = data.getValue0();
		this.constraints = data.getValue1();
		this.ordLine = data.getValue2();
		this.resultFrame = resultFrame;
	}

	/**
	 * Returns the list of variables.
	 * 
	 * @return the list of variables
	 */
	public Set<Variable> getVariables() {
		return variables;
	}

	/**
	 * Returns the list of constraints.
	 * 
	 * @return the list of constraints
	 */
	public Set<Constraint> getConstraints() {
		return constraints;
	}

	/**
	 * Returns the variables order.
	 * 
	 * @return the variables order
	 */
	public List<String> getOrdLine() {
		return ordLine;
	}

	/**
	 * Results the frame used to show computation progression.
	 * 
	 * @return the frame used to show computation progression
	 */
	public ResultFrame getResultFrame() {
		return resultFrame;
	}

	/**
	 * Solves the problem and returns a solution to problem.
	 * 
	 * @return a map containing the solution of the problem. An empty map is
	 *         returned if no solution is found
	 * @throws NotSatisfiableException
	 *             if the problem has no solutions
	 */
	public Map<String, String> solve() throws NotSatisfiableException {
		if (!isSatifiable(Pair.with(variables, constraints), null)) {
			throw new NotSatisfiableException();
		} else {
			Pair<Set<Variable>, Set<Constraint>> consistentCSP = adaptiveConsistency();
			return getSolution(consistentCSP, ordLine);
		}
	}

	/*
	 * Returns variables and constraints obtained by performing the adaptive
	 * consistency algorithm.
	 * 
	 * @return variables and constraints obtained by performing the adaptive
	 * consistency algorithm
	 * 
	 * @throws NotSatisfiableException if the problem has no solutions
	 */
	private Pair<Set<Variable>, Set<Constraint>> adaptiveConsistency()
			throws NotSatisfiableException {
		if (resultFrame != null) {
			resultFrame.updateTextArea("\n\nConsistenza adattiva:", true);
		}

		Pair<Set<Variable>, Set<Constraint>> consistentCSP = Pair.with(
				variables, constraints);

		for (int i = ordLine.size() - 1; i > -1; i--) {
			Variable variable = getVariableFromName(ordLine.get(i),
					consistentCSP.getValue0());
			if (variable != null) {
				// Computing Parents(Var_i)
				List<Variable> parents = getParents(variable,
						consistentCSP.getValue0(), i);

				// Performing consistency (Var_i, Parents(Var_i))
				Constraint newConstraint = consistency(variable, parents,
						consistentCSP.getValue1());

				// Updating the CSP using result of consistency
				consistentCSP = updateCSP(consistentCSP, newConstraint);
				
				if (resultFrame != null) {
					String text = "\n\nIterazione n°" + (ordLine.size() - i)
							+ "\nVariabile: " + ordLine.get(i)
							+ "\nCSP aggiornato:\n\tVariabili:"
							+ consistentCSP.getValue0() + "\n\tVincoli:"
							+ consistentCSP.getValue1();

					resultFrame.updateTextArea(text, true);
				}

				if (!isSatifiable(consistentCSP, newConstraint.getVariables())) {
					throw new NotSatisfiableException();
				}
			}

		}
		return consistentCSP;
	}

	/*
	 * Returns parents of the specified variable according to the ordering.
	 * 
	 * @param variable the variable
	 * 
	 * @param variables the set of variables of the problem
	 * 
	 * @param pos the position of the variable in the ordering
	 * 
	 * @return parents of the specified variable
	 */
	private List<Variable> getParents(final Variable variable,
			final Set<Variable> variables, final int pos) {
		ArrayList<Variable> parents = new ArrayList<>();
		for (int i = 0; i < pos; i++) {
			String currVariableName = ordLine.get(i);

			List<String> variablesNames = new ArrayList<>();
			variablesNames.add(variable.getName());
			variablesNames.add(currVariableName);

			if (existConstraintBetween(variablesNames)) {
				parents.add(getVariableFromName(currVariableName, variables));
			}
		}
		return parents;
	}

	/*
	 * Performs the consistency on the specified variable and its parents and
	 * returns the constraint that involves parents with tuples that are
	 * consistent with the first variable.
	 * 
	 * @param variable the current variable in the algorithm
	 * 
	 * @param parents the parents of the variable
	 * 
	 * @param constraints the set of constraint of the problem
	 * 
	 * @return the constraint involving the parents with tuples that are with
	 * the first variable
	 */
	private Constraint consistency(final Variable variable,
			final List<Variable> parents, final Set<Constraint> constraints) {
		// Getting applicable constraints
		List<Constraint> applicableConstr = getApplicableConstraints(
				constraints, variable.getName(), getNamesFromVariables(parents));

		// Getting all tuples from variables' domains
		List<Variable> allVar = new ArrayList<>();
		allVar.add(variable);
		allVar.addAll(parents);

		List<List<String>> allTuples = getAllTuples(allVar);

		// filter tuples against applicable constraints
		List<List<String>> compTuples = filterTuples(allTuples,
				getNamesFromVariables(allVar), applicableConstr);

		// project compTuples over parents
		int[] positions = new int[parents.size()];
		for (int i = 1; i < allVar.size(); i++) {
			positions[i - 1] = i;
		}
		Set<List<String>> projTuples = projectTuples(compTuples, positions);

		return new Constraint(getNamesFromVariables(parents), projTuples);
	}

	/*
	 * Returns a new CSP obtained by the specified CSP but modified according to
	 * the specified constraint. The modification can be done in three different
	 * ways: 1) the constraint involves one variable, so I used the tuples of
	 * the constraint to modify the domain of that variable. 2) the constraint
	 * involves more that one variable and the list of variables is a
	 * permutation of a list of an existing one, so I used the tuples of the
	 * constraint to modify the tuples of the existing one. 3) the constraint
	 * involves more that one variable and the list is not a permutation of the
	 * list of an existing one, so I add the constraint to the set of
	 * constraints.
	 * 
	 * @param consistentCSP the CSP used to create the new one
	 * 
	 * @param constraint the constraint used in the modification
	 * 
	 * @return the new CSP updated with new information
	 */
	private Pair<Set<Variable>, Set<Constraint>> updateCSP(
			final Pair<Set<Variable>, Set<Constraint>> consistentCSP,
			final Constraint constraint) {
		if (constraint.getVariables().isEmpty()) {
			return consistentCSP;
		}
		if (constraint.getVariables().size() == 1) {
			return computeCSPSingleVariable(consistentCSP, constraint);
		}
		return computeCSPMultipleVariables(consistentCSP, constraint);
	}

	private Pair<Set<Variable>, Set<Constraint>> computeCSPMultipleVariables(Pair<Set<Variable>, Set<Constraint>> consistentCSP, Constraint constraint) {
		// the constraint involves more than one variable, so I use it to
		// update the constraint of the CSP with the same variables or, if
		// it does not exist I add it to CSP's constraints
		Set<Constraint> newConstraints = new HashSet<>();

		Set<Constraint> cspConstraints = consistentCSP.getValue1();

		boolean found = false;
		for (Constraint cspConstraint : cspConstraints) {
			List<String> cspConstrVars = cspConstraint.getVariables();

			if (isPermutationOf(constraint.getVariables(), cspConstrVars)) {
				found = true;
				Constraint permutedConstraint = permuteConstraint(
						constraint, cspConstrVars);

				if (cspConstrVars.equals(permutedConstraint.getVariables())) {

					Set<List<String>> newTuples = setIntersection(
							cspConstraint.getCompTuples(),
							permutedConstraint.getCompTuples());

					newConstraints.add(new Constraint(cspConstraint
							.getVariables(), newTuples));
				}
			} else {
				newConstraints.add(cspConstraint);
			}
		}

		if (!found) {
			newConstraints = new HashSet<>(cspConstraints);
			newConstraints.add(constraint);
		}

		return Pair.with(consistentCSP.getValue0(), newConstraints);
	}

	private Pair<Set<Variable>, Set<Constraint>> computeCSPSingleVariable(Pair<Set<Variable>, Set<Constraint>> consistentCSP, Constraint constraint) {
		// the constraint involves one variable, so I can use it to update
		// the domain of the variable
		String varName = constraint.getVariables().get(0);

		// Because every tuple of the constraint contains only one value, I
		// make a set with the values and I use it in the intersection
		Set<String> domain = new HashSet<>();
		for (List<String> list : constraint.getCompTuples()) {
			domain.add(list.get(0));
		}

		Set<Variable> newVariables = new HashSet<>();

		for (Variable cspVariable : consistentCSP.getValue0()) {
			if (cspVariable.getName().equals(varName)) {
				Set<String> newDomain = setIntersection(domain,
						cspVariable.getDomain());

				newVariables.add(new Variable(varName, newDomain));
			} else {
				newVariables.add(cspVariable);
			}

		}

		return Pair.with(newVariables, consistentCSP.getValue1());
	}

	/*
	 * Checks if the specified CSP is satisfiable. The specified list of
	 * variables' names is used to check the satisfiability of the problem
	 * testing only the constraint with these variables (it is useful during the
	 * adaptive consistency algorithm because only one constraint or variable's
	 * domain changes in each iteration). If the list is <code>null</code> every
	 * variable and constraint is checked.
	 * 
	 * @param csp the CSP to check
	 * 
	 * @param constrVariables the list of variables' names to check, or
	 * <code>null</code> for a complete check
	 * 
	 * @return <code>true</code> if the problem is satisfiable,
	 * <code>false</code> otherwise
	 */
	private boolean isSatifiable(
			final Pair<Set<Variable>, Set<Constraint>> csp,
			final List<String> constrVariables) {
		if (constrVariables == null) {
			return completeSatisfiabilityCheck(csp);
		} else {
			return quickSatisfiabilityCheck(csp, constrVariables);
		}
	}

	/*
	 * Returns a solution of the CSP.
	 * 
	 * @param consistentCSP the CSP
	 * 
	 * @return a solution of the CSP
	 */
	private Map<String, String> getSolution(
			final Pair<Set<Variable>, Set<Constraint>> consistentCSP,
			final List<String> order) {
		Map<String, String> solution = new TreeMap<>();

		for (String variableName : order) {
			Variable variable = getVariableFromName(variableName,
					consistentCSP.getValue0());

			if (variable != null) {
				List<String> solVarNames = new ArrayList<>(solution.keySet());

				List<String> allVarNames = new ArrayList<>(solVarNames);
				allVarNames.add(variableName);

				List<Constraint> appConstraints = getApplicableConstraints(
						consistentCSP.getValue1(), variableName, solVarNames);

				boolean found = false;
				Iterator<String> it = variable.getDomain().iterator();
				while (it.hasNext() && !found) {
					String currElem = it.next();

					List<String> tuple = new ArrayList<>(solution.values());
					tuple.add(currElem);

					if (isAcceptable(tuple, allVarNames, appConstraints)) {
						found = true;
						solution.put(variableName, currElem);
					}
				}
			}
		}
		return solution;
	}

	/*
	 * Checks if the specified variables are involved simultaneously in some
	 * constraints.
	 * 
	 * @param names the name of the variables to check
	 * 
	 * @return <code>true</code> if there exist one constraint that involves
	 * simultaneously the variables, <code>false</code> otherwise
	 */
	private boolean existConstraintBetween(final List<String> names) {
		for (Constraint constraint : constraints) {
			// if the specified variables' names are a subset of the variables'
			// names involved in the constraint then there exist a constraint
			// between the specified variables' names
			if (constraint.getVariables().containsAll(names)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Returns the applicable constraints according to the specified variable
	 * and parents. A constraint is applicable if it involves the specified
	 * variable and a subset (also empty) of parents.
	 * 
	 * @param constraints the set of constraints of the problem
	 * 
	 * @param variable the current variable of the algorithm
	 * 
	 * @param parents the parents of the current variable
	 * 
	 * @return the applicable constraints
	 */
	private List<Constraint> getApplicableConstraints(
			final Set<Constraint> constraints, final String variableName,
			final List<String> parentsNames) {
		List<Constraint> appConstraints = new ArrayList<>();

		List<String> namesList = new ArrayList<>();
		namesList.add(variableName);
		namesList.addAll(parentsNames);

		for (Constraint constraint : constraints) {
			List<String> constVarNamesList = constraint.getVariables();

			if (constVarNamesList.contains(variableName)
					&& namesList.containsAll(constVarNamesList)) {
				appConstraints.add(constraint);
			}
		}
		return appConstraints;
	}

	/*
	 * Returns all the tuples created using domain's value of the specified
	 * values, where each position of the tuple is filled with a value in the
	 * domain of the corresponding list of variables.
	 * 
	 * @param variables the list of variables
	 * 
	 * @return the vector of tuples created using domain's values
	 */
	private List<List<String>> getAllTuples(final List<Variable> variables) {
		if (variables.isEmpty()) {
			// If there are no variables I return a vector with the empty tuple
			List<String> emptyTuple = Collections.emptyList();

			return Collections.singletonList(emptyTuple);
		}

		// I extract the domain of the first variable and the list of variables
		// without the first one from the specified variables.
		Set<String> firstVarDomain = variables.get(0).getDomain();
		List<Variable> varTail = variables.subList(1, variables.size());

		// I obtain tuples for the new list
		List<List<String>> subProbTuples = getAllTuples(varTail);

		// For each tuple in the result of the subproblem I create n tuples,
		// where n is the number of elements in the domain of the first
		// variable, such that the first element of the tuple is one of the
		// elements of the first variable's domain and the rest of the tuple is
		// the current tuple of the subproblem.
		List<List<String>> probTuples = new ArrayList<>();
		for (String value : firstVarDomain) {
			for (List<String> tuple : subProbTuples) {

				List<String> newTuple = new ArrayList<>();
				newTuple.add(value);
				newTuple.addAll(tuple);

				probTuples.add(newTuple);
			}
		}

		return probTuples;
	}

	/*
	 * Returns a set of tuples containing tuples of the specified set that are
	 * acceptable according to all the specified constraints.
	 * 
	 * @param tuples the set of tuples to filter
	 * 
	 * @param variableNames the list of the names of variables involved in the
	 * tuples
	 * 
	 * @param constraints the set of constraints
	 * 
	 * @return the set of acceptable tuples
	 */
	private List<List<String>> filterTuples(final List<List<String>> allTuples,
			final List<String> variableNames, final List<Constraint> constraints) {
		// If there are no constraints to consider or there are no acceptable
		// tuples I return the current list of tuples
		if (constraints.isEmpty() || allTuples.isEmpty()) {
			return allTuples;
		}

		Constraint firstConstraint = constraints.get(0);
		List<Constraint> constraintsTail = constraints.subList(1,
				constraints.size());

		// I obtain the list of tuples that are acceptable according to the
		// constraints except the first
		List<List<String>> filteredTail = filterTuples(allTuples,
				variableNames, constraintsTail);

		// for each tuple in the list returned by the subproblem I test if it is
		// acceptable according to the first constraint. If it is acceptable I
		// add it to the result of the problem.
		List<List<String>> filteredTuples = new ArrayList<>();
		for (List<String> tuple : filteredTail) {
			if (isAcceptable(tuple, variableNames, firstConstraint)) {
				filteredTuples.add(tuple);
			}
		}

		return filteredTuples;
	}

	private boolean isAcceptable(final List<String> tuple,
			final List<String> variableNames, final List<Constraint> constraints) {
		for (Constraint constraint : constraints) {
			if (!isAcceptable(tuple, variableNames, constraint)) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Checks if the specified tuple is acceptable according to the specified
	 * constraint, i.e. if the projection of the tuple over constraint variables
	 * is one of the tuple permitted by the constraint.
	 * 
	 * @param tuple the tuple to check
	 * 
	 * @param variableNames the list of the names of variables involved in the
	 * tuples
	 * 
	 * @param constraint the constraint used
	 * 
	 * @return <code>true</code> if the tuple is acceptable, <code>false</code>
	 * otherwise.
	 */
	private boolean isAcceptable(final List<String> tuple,
			final List<String> variableNames, final Constraint constraint) {
		int[] varNamePos = variablesNamesPositions(variableNames,
				constraint.getVariables());
		List<String> projTuple = projectTuple(tuple, varNamePos);

		for (List<String> constrTuple : constraint.getCompTuples()) {
			if (projTuple.equals(constrTuple)) {
				return true;
			}
		}

		return false;
	}

	/*
	 * Returns a list of the positions of the elements of the second list in the
	 * first one.
	 * 
	 * @param varNames the first list
	 * 
	 * @param constNames the second list
	 * 
	 * @return a list of the positions of the elements of the second list in the
	 * first one
	 */
	private int[] variablesNamesPositions(final List<String> variableNames,
			final List<String> constraintNames) {
		int[] positions = new int[constraintNames.size()];
		for (int j = 0; j < constraintNames.size(); j++) {
			for (int i = 0; i < variableNames.size(); i++) {
				if (variableNames.get(i).equals(constraintNames.get(j))) {
					positions[j] = i;
				}
			}
		}

		return positions;
	}

	/*
	 * Projects every tuple in the specified set over the specified positions
	 * and returns a vector with the projected tuples.
	 * 
	 * @param tuples the vector of tuple to project
	 * 
	 * @param positions the positions used in the projection
	 * 
	 * @return the vector of projected tuples
	 */
	private Set<List<String>> projectTuples(
			final List<List<String>> compTuples, final int[] positions) {
		HashSet<List<String>> projectedTuples = new HashSet<>();
		for (List<String> tuple : compTuples) {
			List<String> projTuple = projectTuple(tuple, positions);

			projectedTuples.add(projTuple);
		}

		return projectedTuples;
	}

	/*
	 * Projects the specified tuple over the specified positions and returns the
	 * projected tuple.
	 * 
	 * @param tuple the tuple to project
	 * 
	 * @param positions the positions used in the projection
	 * 
	 * @return the projected tuple
	 */
	private List<String> projectTuple(final List<String> tuple,
			final int[] positions) {
		List<String> projTuple = new ArrayList<>();

		for (int position : positions) {
			projTuple.add(tuple.get(position));
		}
		return projTuple;
	}

	/*
	 * Checks the satisfiability of the specified CSP i.e. there is no variables
	 * with empty domain or constraint with no tuples.
	 * 
	 * @param csp the CSP to check
	 * 
	 * @return <code>true</code> if the CSP is satisfiable, <code>false</code>
	 * otherwise
	 */
	private boolean completeSatisfiabilityCheck(
			final Pair<Set<Variable>, Set<Constraint>> csp) {
		// checking variables
		for (Variable variable : csp.getValue0()) {
			if (variable.getDomain().isEmpty()) {
				return false;
			}
		}

		// checking constraints
		for (Constraint constraint : csp.getValue1()) {
			if (constraint.getCompTuples().isEmpty()) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Checks the satisfiability of the specified CSP using the specified list
	 * of variables' names. If the list contains only one name, the variables
	 * with that name is checked, otherwise the constraint involving the
	 * variables in the list. Returns true if the variable's domain is not empty
	 * or the constraint has at least one tuple.
	 * 
	 * @param csp the CSP to check
	 * 
	 * @param varNames the list of variables' names used
	 * 
	 * @return <code>true</code> if the CSP is satisfiable, <code>false</code>
	 * otherwise
	 */
	private boolean quickSatisfiabilityCheck(
			final Pair<Set<Variable>, Set<Constraint>> csp,
			final List<String> varNames) {
		if (varNames.size() == 1) {
			Variable variable = getVariableFromName(varNames.get(0),
					csp.getValue0());

			if (variable != null && variable.getDomain().isEmpty()) {
				return false;
			}
		} else {
			Constraint constraint = searchConstraintwithVariables(varNames,
					csp.getValue1());

			if (constraint != null && constraint.getCompTuples().isEmpty()) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Returns if the specified second list is a permutation of the specified
	 * first list
	 * 
	 * @param firstList the first list
	 * 
	 * @param secondList the second list
	 * 
	 * @return <code>true</code> if the first list is a permutation of the
	 * second, <code>false</code> otherwise
	 */
	private <T> boolean isPermutationOf(List<T> firstList, List<T> secondList) {
		if (firstList.size() != secondList.size()) {
			return false;
		}

		return firstList.containsAll(secondList);
	}

	/*
	 * Returns the constraint obtained by a permutation of the specified
	 * constraint according to the specified list a variables names.
	 * 
	 * @param constraint the constraint to permute
	 * 
	 * @param variablesNames the list of variables names
	 * 
	 * @return the constraint after the permutation
	 */
	private Constraint permuteConstraint(final Constraint constraint,
			final List<String> variablesNames) {
		if (variablesNames.equals(constraint.getVariables())) {
			return constraint;
		}

		Map<Integer, Integer> permutationMap = computePermutationMap(
				constraint.getVariables(), variablesNames);

		Set<List<String>> permTuples = new HashSet<>();
		for (List<String> tuple : constraint.getCompTuples()) {
			String[] permTuple = new String[tuple.size()];

			for (Entry<Integer, Integer> mapEntry : permutationMap.entrySet()) {
				permTuple[mapEntry.getValue()] = tuple.get(mapEntry.getKey());
			}
			permTuples.add(Arrays.asList(permTuple));
		}

		return new Constraint(variablesNames, permTuples);
	}

	/*
	 * Computes the permutation map between the specified lists. For each object
	 * in the first list, it maps the position of the object in the first list
	 * in the position of the same object in the second list.
	 * 
	 * @param sourceList the first list
	 * 
	 * @param objectList the second list
	 * 
	 * @return the permutation map
	 */
	private <T> Map<Integer, Integer> computePermutationMap(
			final List<T> sourceList, final List<T> objectList) {
		Map<Integer, Integer> map = new HashMap<>();

		for (int i = 0; i < sourceList.size(); i++) {
			int index = objectList.indexOf(sourceList.get(i));
			if (index != -1) {
				map.put(i, index);
			}
		}

		return map;
	}

	/*
	 * Returns the intersection between the specified sets.
	 * 
	 * @param firstSet the first operand of the intersection
	 * 
	 * @param secondSet the second operand of the intersection
	 * 
	 * @return the intersection between the specified sets
	 */
	private <T> Set<T> setIntersection(Set<T> firstSet, Set<T> secondSet) {
		Set<T> intersection = new HashSet<>();

		Set<T> newFirst;
		Set<T> newSecond;
		if (firstSet.size() <= secondSet.size()) {
			newFirst = firstSet;
			newSecond = secondSet;
		} else {
			newFirst = secondSet;
			newSecond = firstSet;
		}

		for (T element : newFirst) {
			if (newSecond.contains(element)) {
				intersection.add(element);
			}
		}

		return intersection;
	}

	/*
	 * Returns the variable in the specified variables set with the specified
	 * name
	 * 
	 * @param name the name of the variable to find
	 * 
	 * @param variables the set of variables in which search
	 * 
	 * @return the variable with the specified name
	 */
	private Variable getVariableFromName(final String name,
			final Set<Variable> variables) {
		for (Variable variable : variables) {
			if (name.equals(variable.getName())) {
				return variable;
			}
		}
		return null;
	}

	/*
	 * Returns a list of names of the specified variables
	 * 
	 * @param variables the list of variables
	 * 
	 * @return the names of the variables
	 */
	private List<String> getNamesFromVariables(final List<Variable> variables) {
		ArrayList<String> names = new ArrayList<>();
		for (Variable variable : variables) {
			names.add(variable.getName());
		}
		return names;
	}

	/*
	 * Returns the constraint in the specified set with the specified variables.
	 * This method returns also a constraint which list of variables is a
	 * permutation of the specified list.
	 * 
	 * @param variables the variables of the constraint to look for
	 * 
	 * @param constraints the set of constraint in which search
	 * 
	 * @return the constraint with the specified variables, or <code>null</code>
	 * if this constraint does not exist in the set
	 */
	private Constraint searchConstraintwithVariables(List<String> variables,
			Set<Constraint> cspConstraints) {
		for (Constraint constraint : cspConstraints) {
			if (variables.equals(constraint.getVariables())
					|| isPermutationOf(variables, constraint.getVariables())) {
				return constraint;
			}
		}

		return null;
	}
}
