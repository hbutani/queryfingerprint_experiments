package org.hatke.queryfingerprint.model;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Utils {

    public static class Pair<K, V> {
        final public K left;
        final public V right;

        public Pair(K left, V right) {
            this.left = left;
            this.right = right;
        }
    }

    public static <K, V> Pair<K,V> pairOf(K left, V right) {
        return new Pair(left, right);
    }

    public static <E extends Comparable<? super E>> int  compareLists(List<E> l1, List<E> l2) {

        int c = Streams.zip(l1.stream(), l2.stream(),
                (l, r) -> l.compareTo(r)
        ).reduce((a, curr) -> a != 0 ? a : curr).get();

        return c != 0 ? c : (l1.size() - l2.size());
    }

    public static <E extends Comparable<? super E>> ImmutableList<E>
    sortList(ImmutableList<E> l) {

        return l.stream().sorted().collect(ImmutableList.Builder<E>::new,
                ImmutableList.Builder<E>::add,
                (b1, b2) -> b1.addAll(b2.build())).build();
    }

    /**
     * Sort the input list and return mapping of elements to old positions.
     * @param l
     * @return pair of sorted list and original list positions
     * @param <E>
     */
    public static <E extends Comparable<? super E>> Pair<ImmutableList<E>, List<Integer>>
    sortListWithIndexes(ImmutableList<E> l) {

        Supplier<ImmutableList<Integer>> setup = () -> {
            ImmutableList.Builder<Integer> idxList = new ImmutableList.Builder();
            for(int i=0; i < l.size(); i++) idxList.add(i);
            return idxList.build();
        };

        ImmutableList<Integer> idxes = setup.get();

        Stream<Pair<E, Integer>> str = Streams.
                zip(l.stream(), idxes.stream(), (e, i) -> pairOf(e, i)).
                sorted((p1, p2) -> p1.left.compareTo(p2.left));

        ImmutableList.Builder<E> sortedList = new ImmutableList.Builder<>();
        ImmutableList.Builder<Integer> sortIndexes = new ImmutableList.Builder<>();

        str.forEach(p -> {
            sortedList.add(p.left);
            sortIndexes.add(p.right);
        });

        return pairOf(sortedList.build(), sortIndexes.build());
    }

    public static <E extends Comparable<? super E>, T> Pair<ImmutableList<E>, ImmutableList<T>>
    sortLists(ImmutableList<E> l1, ImmutableList<T> l2) {

        Pair<ImmutableList<E>, List<Integer>> sortedL1 = sortListWithIndexes(l1);
        ImmutableList.Builder<T> sortedL2 = new ImmutableList.Builder<>();
        sortedL1.right.forEach(i -> sortedL2.add(l2.get(i)));

        return pairOf(sortedL1.left, sortedL2.build());
    }

    public static <T> String optionalInfoString(String fieldName, Optional<T> val) {
        return val.map(v -> fieldName + "=" + v).orElse("");
    }
}
