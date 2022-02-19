package it.alesc.adaptiveconsistency.logic.csp;

import it.alesc.adaptiveconsistency.specification.ProblemSpecification;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record StartInformation(Set<Variable> variables, Set<Constraint> constraints, List<String> variableOrder) implements Serializable {
    public static StartInformation buildStartInformation(ProblemSpecification specification) {
        var variables = specification.getVariables().stream()
                .map(variable -> new Variable(variable.getName(), variable.getDomainValues()))
                .collect(Collectors.toSet());

        final var constraints = specification.getConstraints().stream()
                .map(constraint -> {
                    var constraintVariables = List.of(constraint.getFirstVariable(), constraint.getSecondVariable());
                    return new Constraint(constraintVariables, variables, constraint.getOperator());
                })
                .collect(Collectors.toSet());
        return new StartInformation(variables, constraints, specification.getVariableOrder());
    }

    public CSP toCSP() {
        return new CSP(variables, constraints);
    }
}
