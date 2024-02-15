package com.minister.component.utils.function;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Tuple3
 *
 * @author QIUCHANGQING620
 * @date 2024-02-12 17:38
 */
@Getter
@AllArgsConstructor
public class Tuple3<T1, T2, T3> implements Iterable<Object> {

    private T1 t1;
    private T2 t2;
    private T3 t3;

    /**
     * Map the 1st part (T1) of this {@link Tuple3} into a different value and type,
     * keeping the other parts.
     *
     * @param mapper the mapping {@link Function} for the T1 part
     * @param <R>    the new type for the T1 part
     * @return a new {@link Tuple3} with a different T1 value
     */
    public <R> Tuple3<R, T2, T3> mapT1(Function<T1, R> mapper) {
        return new Tuple3<>(mapper.apply(t1), t2, t3);
    }

    /**
     * Map the 2nd part (T2) of this {@link Tuple3} into a different value and type,
     * keeping the other parts.
     *
     * @param mapper the mapping {@link Function} for the T2 part
     * @param <R>    the new type for the T2 part
     * @return a new {@link Tuple3} with a different T2 value
     */
    public <R> Tuple3<T1, R, T3> mapT2(Function<T2, R> mapper) {
        return new Tuple3<>(t1, mapper.apply(t2), t3);
    }

    /**
     * Map the 3rd part (T3) of this {@link Tuple3} into a different value and type,
     * keeping the other parts.
     *
     * @param mapper the mapping {@link Function} for the T3 part
     * @param <R>    the new type for the T3 part
     * @return a new {@link Tuple3} with a different T3 value
     */
    public <R> Tuple3<T1, T2, R> mapT3(Function<T3, R> mapper) {
        return new Tuple3<>(t1, t2, mapper.apply(t3));
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
            case 2:
                return t3;
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
        return new Object[]{t1, t2, t3};
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

    public static <T1, T2, T3> Tuple3Builder<T1, T2, T3> builder() {
        return new Tuple3Builder<>();
    }

    public static class Tuple3Builder<T1, T2, T3> {

        private T1 t1;
        private T2 t2;
        private T3 t3;

        public Tuple3Builder<T1, T2, T3> t1(T1 t1) {
            this.t1 = t1;
            return this;
        }

        public Tuple3Builder<T1, T2, T3> t2(T2 t2) {
            this.t2 = t2;
            return this;
        }

        public Tuple3Builder<T1, T2, T3> t3(T3 t3) {
            this.t3 = t3;
            return this;
        }

        public Tuple3<T1, T2, T3> build() {
            return new Tuple3<>(t1, t2, t3);
        }

    }

}
