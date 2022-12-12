package org.hatke.queryfingerprint.model;

import com.google.common.base.Objects;

public class TableReference implements TableSource {

    private final TableName name;

    public TableReference(TableName name) {
        this.name = name;
    }

    public TableName getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableReference that = (TableReference) o;
        return Objects.equal(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    public SourceType sourceType() {
        return SourceType.table;
    }

    public int compareSource(TableSource s) {
        if ( s instanceof TableReference) {
            TableReference t = (TableReference) s;
            return name.compareTo(t.getName());
        } else {
            return compareTo(s);
        }
    }
}
