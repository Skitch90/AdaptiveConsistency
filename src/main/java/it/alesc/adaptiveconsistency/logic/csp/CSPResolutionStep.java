package it.alesc.adaptiveconsistency.logic.csp;

public record CSPResolutionStep(
        int number,
        String variable,
        CSP updatedCSP
) {
}
