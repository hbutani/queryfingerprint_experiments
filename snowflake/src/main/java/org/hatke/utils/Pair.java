package org.hatke.utils;

public class Pair<K, V> {
    final public K left;
    final public V right;

    public Pair(K left, V right) {
        this.left = left;
        this.right = right;
    }

    public static <K, V> Pair<K,V> pairOf(K left, V right) {
        return new Pair(left, right);
    }
}

