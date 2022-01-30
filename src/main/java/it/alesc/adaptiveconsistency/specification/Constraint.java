package it.alesc.adaptiveconsistency.specification;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Constraint {
    private String firstVariable;
    private String secondVariable;
    private Operator operator;
}
