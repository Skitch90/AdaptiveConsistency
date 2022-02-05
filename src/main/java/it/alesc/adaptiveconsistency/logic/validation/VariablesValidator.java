package it.alesc.adaptiveconsistency.logic.validation;

import io.vavr.control.Validation;
import it.alesc.adaptiveconsistency.specification.Variable;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class VariablesValidator {
    public static Validation<String, List<Variable>> validate(final List<Variable> variables) {
        return CollectionValidator.notEmptyList(variables, "Lista variabili")
                .flatMap(VariablesValidator::validateVariableNames);
    }

    private static Validation<String, List<Variable>> validateVariableNames(final List<Variable> variables) {
        final Map<Variable, Long> variableCount = variables.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        final List<String> duplicatedVariableNames = variableCount.entrySet().stream().filter(entry -> entry.getValue() > 1L)
                .map(Map.Entry::getKey).map(Variable::getName).toList();
        return duplicatedVariableNames.isEmpty() ? Validation.valid(variables)
                : Validation.invalid(String.format("Ci sono nomi duplicati in più variabili: %s",
                String.join(", ", duplicatedVariableNames)));
    }
}
