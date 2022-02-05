package it.alesc.adaptiveconsistency.logic.validation;

import io.vavr.control.Validation;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

@UtilityClass
public class CollectionValidator {
    public static <T> Validation<String, List<T>> notEmptyList(List<T> list, String listName) {
        return CollectionUtils.isEmpty(list) ? Validation.invalid(listName + " non presente")
                : Validation.valid(list);
    }
}
