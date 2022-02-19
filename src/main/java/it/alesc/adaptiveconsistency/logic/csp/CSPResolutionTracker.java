package it.alesc.adaptiveconsistency.logic.csp;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public record CSPResolutionTracker(
        StartInformation startInformation,
        List<CSPResolutionStep> iterations,
        boolean hasSolution,
        Map<String, String> solution
) {
    public CSPResolutionTracker(StartInformation startInformation, boolean hasSolution) {
        this(startInformation, List.of(), hasSolution, null);
    }

    public Set<Variable> lastStepVariables() {
        return iterations.isEmpty() ? startInformation.variables()
                : iterations.get(iterations.size() - 1).updatedCSP().variables();
    }

    public Set<Constraint> lastStepConstraints() {
        return iterations.isEmpty() ? startInformation.constraints()
                : iterations.get(iterations.size() - 1).updatedCSP().constraints();
    }

    public int lastStepIndex() {
        return iterations().size();
    }

    public List<String> variablesOrder() {
        return startInformation.variableOrder();
    }

    public CSPResolutionTracker addStep(CSPResolutionStep step, boolean hasSolution) {
        final List<CSPResolutionStep> steps = Stream.concat(iterations.stream(), Stream.of(step)).toList();
        return new CSPResolutionTracker(startInformation, steps, hasSolution, null);
    }

    public CSPResolutionTracker addSolution(Map<String, String> solution) {
        return new CSPResolutionTracker(startInformation, iterations, true, solution);
    }

    public CSPResolutionTracker finish(UnaryOperator<CSPResolutionTracker> finishFunction) {
        return finishFunction.apply(this);
    }
}
