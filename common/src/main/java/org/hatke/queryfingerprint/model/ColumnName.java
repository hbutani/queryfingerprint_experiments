package org.hatke.queryfingerprint.model;

import com.google.common.base.Objects;

public class ColumnName extends Name {

    private static final long serialVersionUID = 3917751473040665897L;

    private final TableName table;

    public ColumnName(TableName t, String name) {
        super(t, name);
        table = t;
    }

    public TableName getTable() {
        return table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ColumnName that = (ColumnName) o;
        return Objects.equal(getTable(), that.getTable());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), getTable());
    }
}
