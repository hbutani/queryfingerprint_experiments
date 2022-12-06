package org.hatke.queryfingerprint.model;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;

/**
 * <b>Design Notes:</b>
 * <p>
 * - ideally would like this to be a Scala Value class that at runtime is
 *   just an ImmutableList<String>
 * </p>
 */
public class Name implements Serializable, Comparable<Name> {

    private static final long serialVersionUID = 5932029451936805802L;

    protected final ImmutableList<String> names;

    public Name(String... names) {
        this.names = ImmutableList.copyOf(names);
    }

    protected Name(Name parent, String name) {
        this.names = ImmutableList.<String>builder().addAll(parent.names).add(name).build();
    }

    public String unqualifiedName() {
        return names.get(names.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Name name = (Name) o;
        return Objects.equal(names, name.names);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(names);
    }

    @Override
    public int compareTo(Name o) {
        return Utils.compareLists(this.names, o.names);
    }
}
