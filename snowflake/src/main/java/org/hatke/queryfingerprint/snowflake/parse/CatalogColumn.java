package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;

import java.util.List;
import java.util.Optional;

public class CatalogColumn implements Column {

    private final int id;
    private final CatalogTable table;

    private final String name;

    private final String fqName;

    CatalogColumn(QueryAnalysis qA, CatalogTable table, TObjectName columnObject) {
        this.id = qA.nextId();
        this.table = table;
        List<String> segments = Utils.normalizedColName(table.getSqlEnv(), columnObject);
        name = segments.get(segments.size() - 1);
        fqName = table.getFqName() + "." + name;
    }

    public TSQLEnv getSqlEnv() {
        return table.getSqlEnv();
    }

    public int getId() {
        return id;
    }

    public CatalogTable getTable() {
        return table;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getFQN() {
        return fqName;
    }

    @Override
    public Source getSource() {
        return table;
    }

    public Optional<Column> asCatalogColumn() {
        return Optional.of(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogColumn that = (CatalogColumn) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
