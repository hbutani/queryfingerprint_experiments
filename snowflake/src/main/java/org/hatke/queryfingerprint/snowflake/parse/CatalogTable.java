package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import org.hatke.utils.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class CatalogTable implements Source {

    private final int id;
    private final TTable tableNode;

    private final String name;

    private final String fqName;


    private final ImmutableList<Column> columns;
    private final ImmutableMap<String, Column> columnMap;


    CatalogTable(QueryAnalysis qA, TTable tableNode) {
        this.id = qA.nextId();
        this.tableNode = tableNode;
        Pair<String, String> p = Utils.fqNormalizedTableName(tableNode.getSqlEnv(), tableNode.getTableName());
        name = p.left;
        fqName = p.right;

        Set<String> nameSet = new HashSet<>();
        ImmutableList.Builder<Column> b = new ImmutableList.Builder();
        ImmutableMap.Builder<String, Column> m = new ImmutableMap.Builder();

        if (tableNode.getLinkedColumns() != null ) {
            for (TObjectName c : tableNode.getLinkedColumns()) {
                CatalogColumn col = new CatalogColumn(qA, this, c);
                if (!nameSet.contains(col.getName())) {
                    nameSet.add(col.getName());
                    b.add(col);
                    m.put(col.getName(), col);
                    m.put(col.getFQN(), col);
                }
            }
        }
        columns = b.build();
        columnMap = m.build();
    }

    public int getId() {
        return id;
    }

    public TTable getTableNode() {
        return tableNode;
    }

    public String getName() {
        return name;
    }

    public String getFqName() {
        return fqName;
    }

    public TSQLEnv getSqlEnv() {
        return tableNode.getSqlEnv();
    }

    @Override
    public Optional<Column> resolveColumn(TObjectName objName) {
        List<String> segments = Utils.normalizedColName(getSqlEnv(), objName);

        if (segments.size() == 1) {
            return Optional.of(columnMap.get(segments.get(0)));
        } else if (segments.size() == 2 && segments.get(0).equals(getName())) {
            return Optional.of(columnMap.get(segments.get(1)));
        } else {
            String nm = segments.stream().collect(Collectors.joining("."));
            return Optional.of(columnMap.get(nm));
        }
    }

    public Iterable<Column> columns() {
        return columns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogTable that = (CatalogTable) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
