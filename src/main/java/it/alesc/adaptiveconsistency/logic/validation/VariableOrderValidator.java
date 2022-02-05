package it.alesc.adaptiveconsistency.logic.validation;

import io.vavr.control.Validation;
import it.alesc.adaptiveconsistency.specification.Variable;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class VariableOrderValidator {
    public static Validation<String, List<String>> validate(List<String> variableOrder, final List<Variable> variables) {
        return CollectionValidator.notEmptyList(variableOrder, "Ordine variabili")
                .flatMap(list -> validateVariablesOrderSize(list, variables))
                .flatMap(list -> validateVariablesOrder(list, variables));
    }

    private static Validation<String, List<String>> validateVariablesOrderSize(List<String> variableOrder, List<Variable> variables) {
        return variableOrder.size() != variables.size()
            ? Validation.invalid("Il numero delle variabili nell'ordinamento è diverso dal numero della variabili dichiarate")
            : Validation.valid(variableOrder);
    }

    private static Validation<String, List<String>> validateVariablesOrder(List<String> variableOrder, List<Variable> variables) {
        final List<String> variableNames = variables.stream().map(Variable::getName).toList();
        final List<String> unknownNames = variableOrder.stream().filter(name -> !variableNames.contains(name)).toList();
        return unknownNames.isEmpty() ? Validation.valid(variableOrder)
                : Validation.invalid(String.format("Nell'ordinamento ci sono nomi di variabili non definite: %s",
                String.join(", ", unknownNames)));
    }
}
