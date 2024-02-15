package com.minister.component.utils.function;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Tuple2
 *
 * @author QIUCHANGQING620
 * @date 2024-02-12 17:36
 */
@Getter
@AllArgsConstructor
public class Tuple2<T1, T2> implements Iterable<Object> {

    private T1 t1;
    private T2 t2;

    /**
     * Map the left-hand part (T1) of this {@link Tuple2} into a different value and type,
     * keeping the right-hand part (T2).
     *
     * @param mapper the mapping {@link Function} for the left-hand part
     * @param <R>    the new type for the left-hand part
     * @return a new {@link Tuple2} with a different left (T1) value
     */
    public <R> Tuple2<R, T2> mapT1(Function<T1, R> mapper) {
        return new Tuple2<>(mapper.apply(t1), t2);
    }

    /**
     * Map the right-hand part (T2) of this {@link Tuple2} into a different value and type,
     * keeping the left-hand part (T1).
     *
     * @param mapper the mapping {@link Function} for the right-hand part
     * @param <R>    the new type for the right-hand part
     * @return a new {@link Tuple2} with a different right (T2) value
     */
    public <R> Tuple2<T1, R> mapT2(Function<T2, R> mapper) {
        return new Tuple2<>(t1, mapper.apply(t2));
    }

    /**
     * Get the object at the given index.
     *
     * @param index The index of the object to retrieve. Starts at 0.
     * @return The object or {@literal null} if out of bounds.
     */
    public Object get(int index) {
        switch (index) {
            case 0:
                return t1;
            case 1:
                return t2;
            default:
                return null;
        }
    }

    /**
     * Turn this {@code Tuple} into a {@link List List&lt;Object&gt;}.
     * The list isn't tied to this Tuple but is a <strong>copy</strong> with limited
     * mutability ({@code add} and {@code remove} are not supported, but {@code set} is).
     *
     * @return A copy of the tuple as a new {@link List List&lt;Object&gt;}.
     */
    public List<Object> toList() {
        return Arrays.asList(toArray());
    }

    /**
     * Turn this {@code Tuple} into a plain {@code Object[]}.
     * The array isn't tied to this Tuple but is a <strong>copy</strong>.
     *
     * @return A copy of the tuple as a new {@link Object Object[]}.
     */
    public Object[] toArray() {
        return new Object[]{t1, t2};
    }

    /**
     * Return an <strong>immutable</strong> {@link Iterator Iterator&lt;Object&gt;} around
     * the content of this {@code Tuple}.
     *
     * @return An unmodifiable {@link Iterator} over the elements in this Tuple.
     * @implNote As an {@link Iterator} is always tied to its {@link Iterable} source by
     * definition, the iterator cannot be mutable without the iterable also being mutable.
     */
    @Override
    public Iterator<Object> iterator() {
        return Collections.unmodifiableList(toList()).iterator();
    }

    public static <T1, T2> Tuple2Builder<T1, T2> builder() {
        return new Tuple2Builder<>();
    }

    public static class Tuple2Builder<T1, T2> {

        private T1 t1;
        private T2 t2;

        public Tuple2Builder<T1, T2> t1(T1 t1) {
            this.t1 = t1;
            return this;
        }

        public Tuple2Builder<T1, T2> t2(T2 t2) {
            this.t2 = t2;
            return this;
        }

        public Tuple2<T1, T2> build() {
            return new Tuple2<>(t1, t2);
        }

    }

}
