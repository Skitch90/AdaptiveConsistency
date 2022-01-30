package it.alesc.adaptiveconsistency.specification;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Variable {
    private String name;
    private Set<String> domainValues;
}
