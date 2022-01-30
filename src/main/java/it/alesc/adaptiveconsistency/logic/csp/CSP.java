package it.alesc.adaptiveconsistency.logic.csp;

import java.util.Set;

public record CSP(Set<Variable> variables, Set<Constraint> constraints) {
}
