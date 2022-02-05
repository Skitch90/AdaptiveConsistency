package it.alesc.adaptiveconsistency.logic.validation;

import io.vavr.control.Validation;
import it.alesc.adaptiveconsistency.specification.Constraint;
import it.alesc.adaptiveconsistency.specification.Variable;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Stream;

@UtilityClass
public class ConstraintsValidator {
    public static Validation<String, List<Constraint>> validate(List<Constraint> constraints, final List<Variable> variables) {
        return CollectionValidator.notEmptyList(constraints, "Lista vincoli")
                .flatMap(list -> validateConstraintVariables(list, variables));
    }

    private static Validation<String, List<Constraint>> validateConstraintVariables(List<Constraint> constraints, List<Variable> variables) {
        final List<String> variableNames = variables.stream().map(Variable::getName).toList();
        final List<String> unknownNames = constraints.stream().flatMap(c -> Stream.of(c.getFirstVariable(), c.getSecondVariable()))
                .distinct().filter(name -> !variableNames.contains(name)).toList();
        return unknownNames.isEmpty() ? Validation.valid(constraints)
                : Validation.invalid(String.format("Nei vincoli ci sono nomi di variabili non definite: %s",
                                                    String.join(", ", unknownNames)));
    }
}
