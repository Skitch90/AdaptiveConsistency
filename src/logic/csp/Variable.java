package logic.csp;

import java.util.Set;

/**
 * It represents a CSP variable.
 * 
 * @author Alessandro Schio
 * @version 4.0 24 Jan 2014
 * 
 */
public class Variable {
	/**
	 * The name of the variable
	 */
	private String name;
	/**
	 * The domain of the variable
	 */
	private Set<String> domain;

	/**
	 * The main constructor of the class. It requires the name of the variable
	 * and its domain.
	 * 
	 * @param name
	 *            the name of the variable
	 * @param domain
	 *            the domain of the variable
	 */
	public Variable(final String name, final Set<String> domain) {
		this.name = name;
		this.domain = domain;
	}

	/**
	 * Returns the name of the variable.
	 * 
	 * @return the name of the variable
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the domain of the variable.
	 * 
	 * @return the domain of the variable
	 */
	public Set<String> getDomain() {
		return domain;
	}

	/**
	 * Sets the domain of the variable
	 * 
	 * @param domain
	 *            the domain to set
	 */
	public void setDomain(Set<String> domain) {
		this.domain = domain;
	}

	@Override
	public String toString() {
		return "<" + name + ", " + domain.toString() + ">";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj == null || !(obj instanceof Variable)) {
			return false;
		}

		return name.equals(((Variable) obj).name)
				&& domain.equals(((Variable) obj).domain);
	}

	@Override
	public int hashCode() {
		return 17 * name.hashCode() + domain.hashCode();
	}
}
