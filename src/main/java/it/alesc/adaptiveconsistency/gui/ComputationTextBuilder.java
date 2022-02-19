package it.alesc.adaptiveconsistency.gui;

import it.alesc.adaptiveconsistency.logic.csp.CSPResolutionStep;
import it.alesc.adaptiveconsistency.logic.csp.CSPResolutionTracker;
import it.alesc.adaptiveconsistency.logic.csp.StartInformation;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@UtilityClass
public class ComputationTextBuilder {
    private static final String START_INFORMATION_FORMAT = """
            Informazioni iniziali:
                            
            Variabili: %s
            Vincoli: %s
            Ordinamento: %s""";
    private static final String COMPUTATION_HEADER = """
                           
                           
            Consistenza adattiva:""";
    public static final String SOLUTION_PATTERN = """
                            
                            
            Soluzione: %s""";
    public static final String NO_SOLUTION_END = """
                            
                            
            Il problema non ha soluzioni""";
    public static final String ITERATION_FORMAT = """
            
            
            Iterazione n°%d
            Variabile: %s
            CSP aggiornato:
                Variabili:%s
                Vincoli:%s""";

    public static String print(CSPResolutionTracker cspResolutionTracker) {
        if (cspResolutionTracker == null) {
            return StringUtils.EMPTY;
        }

        return printStartInformation(cspResolutionTracker.startInformation()) +
                printIterations(cspResolutionTracker.iterations()) +
                printEnding(cspResolutionTracker.hasSolution(), cspResolutionTracker.solution());
    }

    private static String printStartInformation(StartInformation startInformation) {
        return String.format(START_INFORMATION_FORMAT,
                startInformation.variables(), startInformation.constraints(), startInformation.variableOrder());
    }

    private static String printIterations(List<CSPResolutionStep> iterations) {
        return iterations.stream()
                .map(iteration -> String.format(ITERATION_FORMAT,
                        iteration.number(), iteration.variable(),
                        iteration.updatedCSP().variables(), iteration.updatedCSP().constraints()))
                .reduce(COMPUTATION_HEADER,
                        String::concat);
    }

    private static String printEnding(boolean hasSolution, Map<String, String> solution) {
        return hasSolution
                ? String.format(SOLUTION_PATTERN, solution)
                : NO_SOLUTION_END;
    }
}
