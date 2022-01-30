package it.alesc.adaptiveconsistency.logic.csp;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;

/**
 * It represents a CSP variable.
 * 
 * @author Alessandro Schio
 * @version 4.0 24 Jan 2014
 * 
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Variable {
	/**
	 * The name of the variable
	 */
	private String name;
	/**
	 * The domain of the variable
	 */
	private Set<String> domain;

	@Override
	public String toString() {
		return "<" + name + ", " + domain.toString() + ">";
	}
}
