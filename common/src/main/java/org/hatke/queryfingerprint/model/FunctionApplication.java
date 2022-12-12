package org.hatke.queryfingerprint.model;

import com.google.common.base.Objects;

/**
 * Capture that a column is a parameter to a function.
 */
public class FunctionApplication implements Comparable<FunctionApplication> {

    enum FunctionKind {
        numeric, string, datetime, aggregate, collection, other
    }

    private static final long serialVersionUID = 524285943524692588L;

    private final Name function;

    private final FunctionKind kind;
    private final ColumnName column;

    public FunctionApplication(Name function, FunctionKind kind, ColumnName column) {
        this.function = function;
        this.kind = kind;
        this.column = column;
    }

    public Name getFunction() {
        return function;
    }

    public ColumnName getColumn() {
        return column;
    }

    public FunctionKind getKind() {
        return kind;
    }

    @Override
    public int compareTo(FunctionApplication o) {
        int c = this.function.compareTo(o.getFunction());

        if ( c == 0 ) {
            c = this.kind.compareTo(o.kind);
        }

        if ( c == 0 ) {
            c = this.column.compareTo(o.column);
        }

        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionApplication that = (FunctionApplication) o;
        return Objects.equal(getFunction(), that.getFunction()) && getKind() == that.getKind() && Objects.equal(getColumn(), that.getColumn());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getFunction(), getKind(), getColumn());
    }
}
