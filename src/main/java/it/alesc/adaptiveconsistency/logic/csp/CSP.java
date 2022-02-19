package it.alesc.adaptiveconsistency.logic.csp;

import java.util.List;
import java.util.Set;

public record CSP(Set<Variable> variables, Set<Constraint> constraints) {
    /**
     * Checks the satisfiability of the CSP i.e. there is no variables
     * with empty domain or constraint with no tuples.
     *
     * @return <code>true</code> if the CSP is not satisfiable, <code>false</code>
     * otherwise
     */
    public boolean notSatisfiable() {
        return variables.stream().anyMatch(variable -> variable.getDomain().isEmpty())
                || constraints.stream().anyMatch(constraint -> constraint.getCompTuples().isEmpty());
    }

    /**
     * Checks the satisfiability of the CSP using the specified list
     * of variables' names. If the list contains only one name, the variables
     * with that name is checked, otherwise the constraint involving the
     * variables in the list. Returns true if the variable's domain is not empty
     * or the constraint has at least one tuple.
     *
     * @param variableNames the list of variables' names used
     *
     * @return <code>true</code> if the CSP is not satisfiable, <code>false</code>
     * otherwise
     */
    public boolean notSatisfiable(final List<String> variableNames) {
        if (variableNames.size() == 1) {
            return Utils.getVariableFromName(variableNames.get(0), variables)
                    .map(variable -> variable.getDomain().isEmpty()).orElse(false);
        }

        return Utils.searchConstraintWithVariables(variableNames, constraints)
                .map(constraint -> constraint.getCompTuples().isEmpty()).orElse(false);
    }
}