package it.alesc.adaptiveconsistency.logic.exceptions;

import it.alesc.adaptiveconsistency.logic.CategoryLines;

/**
 * It represent the error that occurs when in constraints or in variables order
 * there exists a variable name that does not correspond at any variable
 * previously declared.
 * 
 * @author Alessandro Schio
 * @version 1.0 17 Dec 2013
 * 
 */
public class UnknownVariableException extends Exception {
	private static final long serialVersionUID = -2328166948384826461L;
	private String name;
	private CategoryLines category;

	/**
	 * The default constructor of the class. It requires the specification of
	 * the name of the variable.
	 * 
	 * @param name
	 *            the name of the variable
	 * @param category
	 *            the group in which the error occurs
	 */
	public UnknownVariableException(final String name,
			final CategoryLines category) {
		this.name = name;
		this.category = category;
	}

	/**
	 * Returns the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the group of lines in which the error occurs.
	 * 
	 * @return the group of lines in which the error occurs
	 */
	public CategoryLines getCategory() {
		return category;
	}
}
