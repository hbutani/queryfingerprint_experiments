package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a column of a {@link QB}. We capture 2 features of this column:
 * <li>The input columns referenced in its expression</li>
 * <li>Optionally if it is just an alias of some input Column.</li>
 */
public class QBOutColumn implements Column {

    private final QB qb;
    private final int id;

    private final String alias;

    private final ImmutableList<Column> dependColumns;

    private final Optional<Column> inputColumn;

    public QBOutColumn(QB qb, int id, String alias,
                       ImmutableList<Column> dependColumns,
                       Optional<Column> inputColumn) {
        this.qb = qb;
        this.id = id;
        this.alias = alias;
        this.dependColumns = dependColumns;
        this.inputColumn = inputColumn;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return alias;
    }

    @Override
    public String getFQN() {
        return alias;
    }

    @Override
    public Source getSource() {
        return qb;
    }

    public Optional<Column> asCatalogColumn() {
        return inputColumn.flatMap(c -> c.asCatalogColumn());
    }

    public Optional<QB> appearsInQB() {
        return Optional.of(qb);
    }

    public ImmutableList<Column> getDependColumns() {
        return dependColumns;
    }

    public Optional<Column> replaceByInputColumn() {
        return inputColumn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QBOutColumn that = (QBOutColumn) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
