package logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logic.csp.Constraint;
import logic.csp.Variable;
import logic.exceptions.DuplicateVariableNameException;
import logic.exceptions.UnknownVariableException;
import logic.exceptions.WrongVariablesNumberException;

import org.javatuples.Triplet;

/**
 * This class is used to parsing the input file and obtain data that will be
 * processed.
 * 
 * @author Alessandro Schio
 * @version 6.0 05 Jan 2014
 */
public class InputParser {
	/**
	 * The path of the file to process.
	 */
	private String path;

	/**
	 * The main constructor of the class. It requires the path of the input file.
	 * 
	 * @param path
	 *            the path of the input file that will be parsed
	 */
	public InputParser(final String path) {
		this.path = path;
	}

	/**
	 * Parses the file with the specified path and returns the result with a
	 * tuple containing, constraints and variables order<br />
	 * The file is composed by three parts each separated by a blank line: the
	 * variable section, the constraint section and an order of the variables.<br />
	 * The variable section is a sequence of lines each one specifies
	 * information about a variable. The syntax of a single line is:<br />
	 * <b><i>var_name</i> : {<i>domain_val_1</i>,<i>domain_val2</i>,...}</b><br />
	 * The constraint section is a sequence of lines each one specifies
	 * information about a constraint. The syntax of a single line is:<br />
	 * <b><i>var_name1</i>-<i>varname2</i>:<i>operator</i></b><br />
	 * The last part contains a permutation of variable names separated by "-".
	 * 
	 * @return a tuple containing variables, constraints and variables order
	 * 
	 * @throws ParseException
	 *             if the input file do not follow the syntax
	 * @throws IOException
	 *             if an error occurs while reading the file
	 * @throws DuplicateVariableNameException
	 *             if one or more variables have the same name
	 * @throws UnknownVariableException
	 *             if there exists one or more variables names in constraints
	 *             that don't appear in the variables declaration
	 * @throws WrongVariablesNumberException
	 *             if the variables order is not complete (i.e. some variable is
	 *             missing)
	 */
	public Triplet<Set<Variable>, Set<Constraint>, List<String>> parseFile()
			throws ParseException, IOException, DuplicateVariableNameException,
			UnknownVariableException, WrongVariablesNumberException {
		Triplet<Vector<String>, Vector<String>, String> triplet = groupLines();

		Set<Variable> variables = createVariablesSet(triplet.getValue0());
		checkVariableNames(variables);

		Set<Constraint> constraints = createConstraintsSet(triplet.getValue1(),
				variables);
		checkConstaintVariables(constraints, variables);

		List<String> varOrder = Arrays.asList(createVariablesOrder(triplet
				.getValue2()));
		checkVariablesOrder(varOrder, variables);

		return Triplet.with(variables, constraints, varOrder);
	}

	/*
	 * Groups lines of the file in base of their content and puts each group in
	 * a structure, one of the specified, to identify lines content and returns
	 * a tuple containing computed groups.
	 * 
	 * @return a triplet containing groups
	 * 
	 * @throws IOException if an error occurs during file reading
	 */
	private Triplet<Vector<String>, Vector<String>, String> groupLines()
			throws IOException {
		Vector<String> varLines = new Vector<String>();
		Vector<String> constrLines = new Vector<String>();
		String ordLine = null;

		BufferedReader reader = new BufferedReader(new FileReader(
				new File(path)));

		// For-loop to group the lines of the file
		String line = reader.readLine();
		boolean endFirst = false, endSecond = false, finish = false;
		while (line != null && !finish) {
			if (line.equals("")) {
				// A section of file file ends, so next lines must be grouped in
				// another group
				if (endFirst == false) {
					endFirst = true;
				} else {
					if (endSecond == false) {
						endSecond = true;
					}
				}
			} else {
				if (endSecond == true) {
					// the second section is finished, so the line must be the
					// order line
					ordLine = line;
					finish = true;
				} else {
					// I'm in the first section or in the second one
					if (endFirst == true) {
						// the first section is finished, so I'm the second
						// section
						constrLines.add(line);
					} else {
						// I'm in the first section
						varLines.add(line);
					}
				}
			}

			line = reader.readLine();
		}

		reader.close();

		return Triplet.with(varLines, constrLines, ordLine);
	}

	/*
	 * Returns the set of Variables obtained by examining specified strings that
	 * contain variables information.
	 * 
	 * @param varLines the set of strings with variables information
	 * 
	 * @return the set of Variables resulting from the examination of strings
	 * 
	 * @throws ParseException if one or more strings do not follow the syntax
	 */
	private Set<Variable> createVariablesSet(final Vector<String> varLines)
			throws ParseException {
		Set<Variable> variables = new HashSet<Variable>();

		Pattern varPattern = Pattern.compile("\\w+\\:\\{\\w+(,\\w+)*\\}");
		for (String varLine : varLines) {
			Matcher m = varPattern.matcher(varLine);
			if (m.matches()) {
				String name = varLine.split("\\:")[0];
				String valuesStr = varLine.split("\\:")[1];

				// I remove curly brackets from the string that contains
				// domain's values and then I split the new string around the
				// ',' to obtain singular values
				valuesStr = valuesStr.substring(1, valuesStr.length() - 1);
				String[] values = valuesStr.split(",");

				HashSet<String> valuesSet = new HashSet<>();
				for (String value : values) {
					valuesSet.add(value);
				}

				variables.add(new Variable(name, valuesSet));
			} else {
				throw new ParseException(
						"La riga \""
								+ varLine
								+ "\" deve essere nella forma nome_variabile:{valore1,...,valoreN}",
						0);
			}
		}

		return variables;
	}

	/*
	 * Returns the set of Constraints obtained by examining the specified
	 * strings that contain constraints information.
	 * 
	 * @param constrLines the set of strings with Constraints information
	 * 
	 * @return the set of Constraints resulting from the examination of strings
	 * 
	 * @throws ParseException if one or more strings do not follow the syntax
	 */
	private Set<Constraint> createConstraintsSet(
			final Vector<String> constrLines, final Set<Variable> variables)
			throws ParseException {
		Set<Constraint> constraints = new HashSet<>();

		Pattern constrPattern = Pattern.compile("\\w+\\-\\w+\\:(\\=|\\!\\=)");

		for (String ConstrLine : constrLines) {
			Matcher m = constrPattern.matcher(ConstrLine);
			if (m.matches()) {
				String varStr = ConstrLine.split("\\:")[0];
				String op = ConstrLine.split("\\:")[1];

				List<String> variableNames = Arrays.asList(varStr.split("\\-"));

				constraints.add(new Constraint(variableNames, variables, op));
			} else {
				throw new ParseException(
						"La riga \""
								+ ConstrLine
								+ "\" deve essere nella forma var1-var2:= o var1-var2:!=",
						0);
			}
		}

		return constraints;
	}

	/*
	 * Returns the list of variable names ordered in some way.
	 * 
	 * @param ordLine the string that contains the variables order
	 * 
	 * @return the list of variable names ordered in some way
	 * 
	 * @throws ParseException if the specified string does not follow the syntax
	 */
	private String[] createVariablesOrder(final String ordLine)
			throws ParseException {
		String[] varOrder = null;

		if (ordLine == null) {
			throw new ParseException("La riga dell'ordinamento e` assente", 0);
		} else {
			if (!Pattern.matches("\\w+(\\-\\w+)*", ordLine)) {
				throw new ParseException(
						"La riga dell'ordinamento dev essere nella forma var1-...varN",
						0);
			} else {
				varOrder = ordLine.split("\\-");
			}
		}

		return varOrder;
	}

	/*
	 * Checks if variable names are all different.
	 * 
	 * @param variables the variables to check
	 * 
	 * @throws DuplicateVariableName if there are at least two variables with
	 * the same name
	 */
	private void checkVariableNames(final Set<Variable> variables)
			throws DuplicateVariableNameException {
		// I create a map where I store for each variable's name the number of
		// occurrences of the name.
		Map<String, Integer> namesMap = new HashMap<>();
		for (Iterator<Variable> it = variables.iterator(); it.hasNext();) {
			String name = it.next().getName();
			if (namesMap.containsKey(name)) {
				namesMap.put(name, namesMap.get(name) + 1);
			} else {
				namesMap.put(name, 1);
			}
		}

		for (Entry<String, Integer> nameEntry : namesMap.entrySet()) {
			if (nameEntry.getValue() > 1) {
				throw new DuplicateVariableNameException(nameEntry.getKey());
			}
		}
	}

	/*
	 * Checks if the variables involved in constraints are previously declared
	 * as variables.
	 * 
	 * @param constraints the list of constraints to check
	 * 
	 * @param variables the list of variables
	 * 
	 * @throws UnknownVariableException if there is one variable not previously
	 * declared
	 */
	private void checkConstaintVariables(final Set<Constraint> constraints,
			final Set<Variable> variables) throws UnknownVariableException {
		for (Constraint constraint : constraints) {
			for (String constrVarName : constraint.getVariables()) {
				if (!existsVariableWithName(variables, constrVarName)) {
					throw new UnknownVariableException(constrVarName,
							CategoryLines.Constraints);
				}
			}

		}
	}

	/*
	 * Checks if the variables order contains all the variables declared
	 * previously.
	 * 
	 * @param varOrder the variables order to check
	 * 
	 * @param variables the list of variables
	 * 
	 * @throws WrongVariablesNumberException if the number of variables in the
	 * order is different to the number of variables declared
	 * 
	 * @throws UnknownVariableException if there is one variable not previously
	 * declared
	 */
	private void checkVariablesOrder(final List<String> varOrder,
			final Set<Variable> variables)
			throws WrongVariablesNumberException, UnknownVariableException {
		if (varOrder.size() != variables.size()) {
			throw new WrongVariablesNumberException();
		}

		for (String variableName : varOrder) {
			if (!existsVariableWithName(variables, variableName)) {
				throw new UnknownVariableException(variableName,
						CategoryLines.OrderLine);
			}
		}
	}

	/*
	 * Returns if there exists a variable with the specified name in a set.
	 * 
	 * @param variables the set of variables
	 * 
	 * @param variableName the variable name to search
	 * 
	 * @return <code>true</code> if there exist a variable with the specified
	 * name, <code>false</code> otherwise
	 */
	private boolean existsVariableWithName(final Set<Variable> variables,
			final String variableName) {
		boolean found = false;
		for (Iterator<Variable> it = variables.iterator(); it.hasNext()
				&& !found;) {
			found = variableName.equals(it.next().getName());
		}

		return found;
	}
}