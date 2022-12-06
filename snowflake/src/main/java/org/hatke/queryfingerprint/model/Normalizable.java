package org.hatke.queryfingerprint.model;

import com.google.common.collect.ImmutableList;

import java.util.Objects;

/**
 * A Normalized {@link Queryfingerprint} enables comparing query fingerprints.
 * Normalizing involves:
 * - ordering <code>tableSources</code> such that <code>from A, B</code> and <code>from b, a</code>
 *   have the same fingerprint.
 * - ordering <code>projectedColumns</code> such that <code>select A, B</code> and <code>select b, a</code>
 *   have the same fingerprint.
 * - ordering <code>joins</code> such that <code>from a join b join c</code> and <code>from c join b join a</code>
 *   have the same fingerprint.
 * - ordering individual {@link Join} such that <code>a join b</code> and <code>b join a</code> have the same
 *   fingerprint.
 * - and so on... TODO complete documentation.
 * @param <T>
 */
public interface Normalizable<T> {

    T normalize();

    static <E extends Comparable<? super E>> ImmutableList<E>
    normalize(ImmutableList<E> l) {
        return Utils.sortList(l);
    }

    static <E> E normalize(Normalizable<E> obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        return obj.normalize();
    }

    static <E> E normalize(E obj) {
        return obj;
    }
}
