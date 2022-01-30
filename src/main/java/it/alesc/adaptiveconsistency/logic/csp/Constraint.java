package it.alesc.adaptiveconsistency.logic.csp;

import it.alesc.adaptiveconsistency.specification.Operator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static it.alesc.adaptiveconsistency.specification.Operator.EQUALS;
import static it.alesc.adaptiveconsistency.specification.Operator.NOT_EQUALS;

/**
 * It represents a CSP constraint.
 * 
 * @author Alessandro Schio
 * @version 4.0 05 Jan 2014
 * 
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Constraint {
	/**
	 * Variables involved in the constraint.
	 */
	private final List<String> variables;
	/**
	 * Tuples that are admitted by the constraint.
	 */
	private final Set<List<String>> compTuples;

	/**
	 * The constructor of the class that requires the name of the variables
	 * involved in the constraint, the set of variables of the CSP and the
	 * operator of the constraint.
	 * 
	 * @param constraintVariables
	 *            the name of the variables involved in the constraint
	 * @param variables
	 *            the set of the variables of the CSP
	 * @param operator
	 *            the operator of the constraint
	 */
	public Constraint(List<String> constraintVariables, Set<Variable> variables, Operator operator) {
		this.variables = constraintVariables;
		this.compTuples = computeTuples(constraintVariables, variables, operator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "<" + variables.toString() + ", " + compTuples.toString() + ">";
	}

	/*
	 * Finds and returns the variable with the specified name in the specified
	 * set of variables.
	 * 
	 * @param variables the set of variables
	 * 
	 * @param name the name of variable to find
	 * 
	 * @return the variable with the specified name or <code>null</code> if that
	 * variable does not exist in the set
	 */
	private Variable searchVariableByName(final Set<Variable> variables,
			final String name) {
		for (Variable variable : variables) {
			if (variable.getName().equals(name)) {
				return variable;
			}
		}

		return null;
	}

	/*
	 * Returns a set of tuples generated from elements of domains of the
	 * specified variables. Every tuple is generated to satisfy the property
	 * that every pair of values of the tuple satisfy the operator.
	 * 
	 * @param variableNames the name of the variables which domain's elements
	 * are used to generate the tuples
	 * 
	 * @param variables the set of all variables of the CSP
	 * 
	 * @param op the operator used in the property
	 * 
	 * @return the set of tuples that satisfy the property
	 */
	private HashSet<List<String>> computeTuples(
			final List<String> variableNames, final Set<Variable> variables,
			final Operator op) {
		HashSet<List<String>> constrCompTuples = new HashSet<>();

		if (variableNames.isEmpty()) {
			return constrCompTuples;
		}
		/*
		 * I extract the variable which name is the first element of the names
		 * array, and I create another array that is a copy of the names array
		 * but without the first element.
		 */
		Variable firstVar = searchVariableByName(variables,
				variableNames.get(0));

		if (firstVar == null) {
			return constrCompTuples;
		}

		Set<String> firstDomain = firstVar.getDomain();

		/*
		 * I use the array created by copy to create the tuples considering
		 * variables in the array.
		 */
		List<String> variableNamesTail = variableNames.subList(1,
				variableNames.size());

		HashSet<List<String>> tuples = computeTuples(variableNamesTail,
				variables, op);

		/*
		 * for any tuple of the result of the recursive call I generate
		 * tuples adding an element of the first element domain but keeping
		 * the property satisfied.
		 */
		for (String elemDomain : firstDomain) {
			if (tuples.isEmpty()) {
				constrCompTuples.add(Collections.singletonList(elemDomain));
			} else {
				for (List<String> tuple : tuples) {
					List<String> newTuple = createNewTuple(elemDomain,
							tuple, op);
					if (!CollectionUtils.isEmpty(newTuple)) {
						constrCompTuples.add(newTuple);
					}
				}
			}
		}

		return constrCompTuples;
	}

	/*
	 * Returns the tuple obtained by adding the specified value at the beginning
	 * of the specified tuple. The new tuple is constructed if the property that
	 * for any e element of the tuple "value op e" is satisfied.
	 * 
	 * @param value the value to add
	 * 
	 * @param tuple the tuple to which the value
	 * 
	 * @param op the operator
	 * 
	 * @return the new tuple or <code>null</code> if adding the new value does
	 * not keep the property
	 */
	private List<String> createNewTuple(final String value,
			final List<String> tuple, final Operator op) {
		boolean ok = true;
		for (String elemTuple : tuple) {
			if (op == EQUALS) {
				if (!value.equals(elemTuple)) {
					ok = false;
				}
			} else if (op == NOT_EQUALS) {
				if (value.equals(elemTuple)) {
					ok = false;
				}
			} else {
				ok = false;
			}
		}

		if (ok) {
			/*
			 * The property is satisfied for any element of the tuple, so the
			 * new tuple is constructed
			 */
			List<String> resTuple = new ArrayList<>();
			resTuple.add(value);
			resTuple.addAll(tuple);

			return resTuple;
		} else {
			return Collections.emptyList();
		}
	}
}
