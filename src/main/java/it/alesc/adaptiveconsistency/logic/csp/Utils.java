package it.alesc.adaptiveconsistency.logic.csp;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@UtilityClass
public class Utils {
    /**
	 * Returns the variable in the specified variables set with the specified
	 * name
	 *
	 * @param name the name of the variable to find
	 *
	 * @param variables the set of variables in which search
	 *
	 * @return the variable with the specified name
	 */
	public static Optional<Variable> getVariableFromName(final String name,
												   final Set<Variable> variables) {
		return variables.stream().filter(variable -> StringUtils.equals(variable.getName(), name)).findFirst();
	}

	/**
	 * Returns the constraint in the specified set with the specified variables.
	 * This method returns also a constraint which list of variables is a
	 * permutation of the specified list.
	 *
	 * @param variables the variables of the constraint to look for
	 *
	 * @param constraints the set of constraint in which search
	 *
	 * @return the constraint with the specified variables
	 */
	public static Optional<Constraint> searchConstraintWithVariables(List<String> variables,
															   Set<Constraint> constraints) {
		return constraints.stream()
				.filter(constraint -> CollectionUtils.isEqualCollection(variables, constraint.getVariables()))
				.findFirst();
	}
}
