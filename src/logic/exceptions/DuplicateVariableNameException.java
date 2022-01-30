package logic.exceptions;

/**
 * It represents the error that occurs when 2 or more variables have the same
 * name.
 * 
 * @author Alessandro Schio
 * @version 1.0 06 Nov 2103
 * 
 */
public class DuplicateVariableNameException extends Exception {
	private static final long serialVersionUID = 8270071998227457822L;
	private String name;

	/**
	 * The default constructor of the class. It requires the specification of
	 * the duplicated name.
	 * 
	 * @param name
	 *            the duplicated name
	 */
	public DuplicateVariableNameException(final String name) {
		this.name = name;
	}

	/**
	 * Returns the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

}
