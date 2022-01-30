package it.alesc.adaptiveconsistency.logic.csp;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public record StartInformation(Set<Variable> variables, Set<Constraint> constraints, List<String> variableOrder) implements Serializable {
}
