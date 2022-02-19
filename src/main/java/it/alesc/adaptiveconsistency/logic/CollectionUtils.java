package it.alesc.adaptiveconsistency.logic;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class CollectionUtils {
    /**
     * Creates a list with the given element in the first position and the elements of the given collection after
     *
     * @param element the element to add first
     * @param collection the rest elements
     * @param <T> the type of the elements in the list
     * @return a list with the given element in the first position and the elements of the given collection after
     */
    public static <T> List<T> listOf(T element, Collection<T> collection) {
        return Stream.concat(Stream.of(element), collection.stream()).toList();
    }

    /**
     * Creates a set with the elements of the given collection first and the given element in the last position
     *
     * @param element the element to add last
     * @param collection the start elements
     * @param <T> the type of the elements in the set
     * @return a set with the elements of the given collection first and the given element in the last position
     */
    public static <T> Set<T> setOf(Collection<T> collection, T element) {
        return Stream.concat(collection.stream(), Stream.of(element)).collect(Collectors.toSet());
    }

    /**
     * Creates a list with the elements of the given collection first and the given element in the last position
     *
     * @param element the element to add last
     * @param collection the start elements
     * @param <T> the type of the elements in the list
     * @return a list with the elements of the given collection first and the given element in the last position
     */
    public static <T> List<T> listOf(Collection<T> collection, T element) {
        return Stream.concat(collection.stream(), Stream.of(element)).toList();
    }

    /**
     * Returns <tt>true</tt> iff the given first {@link Collection} is a permutation of the given second one
     *
     * @param first  the first collection, must not be null
     * @param second the second collection, must not be null
     * @param <T> the type of the list
     * @return Returns <tt>true</tt> iff the given first collection is a permutation of the given second one
     */
    public static <T> boolean isPermutation(Collection<T> first, Collection<T> second) {
        return org.apache.commons.collections.CollectionUtils.isEqualCollection(first, second);
    }

    /**
     * Returns every possible list that can be formed by choosing one element from each of the given
     * lists in order; the "n-ary <a href="http://en.wikipedia.org/wiki/Cartesian_product">Cartesian
     * product</a>" of the lists.
     *
     * @param first the first list
     * @param second the second list
     * @param <T> type of first list's elements
     * @param <U> type of second list's elements
     * @return every possible list that can be formed by choosing one element from each of the given
     * lists in order
     */
    public static <T, U> List<Tuple2<T, U>> cartesianProduct(List<T> first, List<U> second) {
        return first.stream()
                .flatMap(element -> createTuple(element, second).stream())
                .toList();
    }

    private static <T, U> List<Tuple2<T, U>> createTuple(T element, List<U> second) {
        return second.stream().map(current -> Tuple.of(element, current)).toList();
    }
}
