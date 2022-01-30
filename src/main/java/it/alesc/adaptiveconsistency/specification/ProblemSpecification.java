package it.alesc.adaptiveconsistency.specification;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ProblemSpecification {
    private List<Variable> variables;
    private List<Constraint> constraints;
    private List<String> variableOrder;
}
