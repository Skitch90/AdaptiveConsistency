package it.alesc.adaptiveconsistency.logic.validation;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import it.alesc.adaptiveconsistency.specification.ProblemSpecification;
import it.alesc.adaptiveconsistency.specification.Variable;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ProblemSpecificationValidator {
	public static Validation<Seq<String>, ProblemSpecification> validate(ProblemSpecification problemSpecification) {
		final List<Variable> variables = problemSpecification.getVariables();
		return Validation.combine(
				VariablesValidator.validate(variables),
				ConstraintsValidator.validate(problemSpecification.getConstraints(), variables),
				VariableOrderValidator.validate(problemSpecification.getVariableOrder(), variables)
		).ap(ProblemSpecification::new);
	}

}
