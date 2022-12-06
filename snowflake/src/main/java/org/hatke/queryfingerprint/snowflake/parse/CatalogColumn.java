package org.hatke.queryfingerprint.snowflake.parse;

import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import org.hatke.utils.Pair;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

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
}
