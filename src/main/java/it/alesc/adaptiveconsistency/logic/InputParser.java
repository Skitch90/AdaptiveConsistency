package it.alesc.adaptiveconsistency.logic;

import com.google.gson.Gson;
import it.alesc.adaptiveconsistency.logic.csp.Constraint;
import it.alesc.adaptiveconsistency.logic.csp.StartInformation;
import it.alesc.adaptiveconsistency.logic.csp.Variable;
import it.alesc.adaptiveconsistency.logic.exceptions.DuplicateVariableNameException;
import it.alesc.adaptiveconsistency.logic.exceptions.UnknownVariableException;
import it.alesc.adaptiveconsistency.logic.exceptions.WrongVariablesNumberException;
import it.alesc.adaptiveconsistency.specification.ProblemSpecification;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * This class is used to parsing the input file and obtain data that will be
 * processed.
 *
 * @author Alessandro Schio
 * @version 6.0 05 Jan 2014
 */
public class InputParser {

	/**
	 * The path of the file to process.
	 */
	private final String path;

	/**
	 * The main constructor of the class. It requires the path of the input file.
	 *
	 * @param path
	 *            the path of the input file that will be parsed
	 */
	public InputParser(final String path) {
		this.path = path;
	}

	/**
	 * Parses the file with the specified path and returns the result with a
	 * tuple containing, constraints and variables order<br />
	 * The file is composed by three parts each separated by a blank line: the
	 * variable section, the constraint section and an order of the variables.<br />
	 * The variable section is a sequence of lines each one specifies
	 * information about a variable. The syntax of a single line is:<br />
	 * <b><i>var_name</i> : {<i>domain_val_1</i>,<i>domain_val2</i>,...}</b><br />
	 * The constraint section is a sequence of lines each one specifies
	 * information about a constraint. The syntax of a single line is:<br />
	 * <b><i>var_name1</i>-<i>varname2</i>:<i>operator</i></b><br />
	 * The last part contains a permutation of variable names separated by "-".
	 *
	 * @return a tuple containing variables, constraints and variables order
	 *
	 * @throws ParseException
	 *             if the input file do not follow the syntax
	 * @throws IOException
	 *             if an error occurs while reading the file
	 * @throws DuplicateVariableNameException
	 *             if one or more variables have the same name
	 * @throws UnknownVariableException
	 *             if there exists one or more variables names in constraints
	 *             that don't appear in the variables declaration
	 * @throws WrongVariablesNumberException
	 *             if the variables order is not complete (i.e. some variable is
	 *             missing)
	 */
	public StartInformation parseFile()
			throws ParseException, IOException, DuplicateVariableNameException,
			UnknownVariableException, WrongVariablesNumberException {
		final String specificationString = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
		final ProblemSpecification specification = new Gson().fromJson(specificationString, ProblemSpecification.class);

		final Set<Variable> variables = specification.getVariables().stream()
				.map(variable -> new Variable(variable.getName(), variable.getDomainValues()))
				.collect(Collectors.toSet());
		checkVariableNames(variables);

		final Set<Constraint> constraints = specification.getConstraints().stream()
				.map(constraint -> {
					var constraintVariables = List.of(constraint.getFirstVariable(), constraint.getSecondVariable());
					return new Constraint(constraintVariables, variables, constraint.getOperator());
				})
				.collect(Collectors.toSet());
		checkConstraintVariables(constraints, variables);

		checkVariablesOrder(specification.getVariableOrder(), variables);

		return new StartInformation(variables, constraints, specification.getVariableOrder());
	}

	/*
	 * Checks if variable names are all different.
	 *
	 * @param variables the variables to check
	 *
	 * @throws DuplicateVariableName if there are at least two variables with
	 * the same name
	 */
	private void checkVariableNames(final Set<Variable> variables)
			throws DuplicateVariableNameException {
		// I create a map where I store for each variable's name the number of
		// occurrences of the name.
		Map<String, Integer> namesMap = new HashMap<>();
		for (Variable variable : variables) {
			String name = variable.getName();
			if (namesMap.containsKey(name)) {
				namesMap.put(name, namesMap.get(name) + 1);
			} else {
				namesMap.put(name, 1);
			}
		}

		for (Entry<String, Integer> nameEntry : namesMap.entrySet()) {
			if (nameEntry.getValue() > 1) {
				throw new DuplicateVariableNameException(nameEntry.getKey());
			}
		}
	}

	/*
	 * Checks if the variables involved in constraints are previously declared
	 * as variables.
	 *
	 * @param constraints the list of constraints to check
	 *
	 * @param variables the list of variables
	 *
	 * @throws UnknownVariableException if there is one variable not previously
	 * declared
	 */
	private void checkConstraintVariables(final Set<Constraint> constraints,
										  final Set<Variable> variables) throws UnknownVariableException {
		for (Constraint constraint : constraints) {
			for (String constrVarName : constraint.getVariables()) {
				if (noVariableWithName(constrVarName, variables)) {
					throw new UnknownVariableException(constrVarName,
							CategoryLines.CONSTRAINTS);
				}
			}

		}
	}

	/*
	 * Checks if the variables order contains all the variables declared
	 * previously.
	 *
	 * @param varOrder the variables order to check
	 *
	 * @param variables the list of variables
	 *
	 * @throws WrongVariablesNumberException if the number of variables in the
	 * order is different to the number of variables declared
	 *
	 * @throws UnknownVariableException if there is one variable not previously
	 * declared
	 */
	private void checkVariablesOrder(final List<String> varOrder,
			final Set<Variable> variables)
			throws WrongVariablesNumberException, UnknownVariableException {
		if (varOrder.size() != variables.size()) {
			throw new WrongVariablesNumberException();
		}

		for (String variableName : varOrder) {
			if (noVariableWithName(variableName, variables)) {
				throw new UnknownVariableException(variableName,
						CategoryLines.ORDER_LINE);
			}
		}
	}

	/*
	 * Returns if there exists a variable with the specified name in a set.
	 *
	 * @param variables the set of variables
	 *
	 * @param variableName the variable name to search
	 *
	 * @return <code>true</code> if there exist a variable with the specified
	 * name, <code>false</code> otherwise
	 */
	private boolean noVariableWithName(final String variableName, final Set<Variable> variables) {
		return variables.stream().noneMatch(variable -> variable.getName().equals(variableName));
	}
}
