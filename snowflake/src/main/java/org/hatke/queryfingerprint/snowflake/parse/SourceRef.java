package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import gudusoft.gsqlparser.util.SQLUtil;
import org.hatke.utils.Pair;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class SourceRef implements Source {

    private final int id;
    final Source source;

    private final Source inSource;

    final Optional<String> alias;
    final Optional<String> fqAlias;

    private final ImmutableList<Column> columns;
    private final ImmutableMap<String, Column> columnMap;

    private String buildFQN(Source source, String tableNm, String normalizedFQN) {
        return
                source.getFQN().map(fqn -> {
                    List<String> segments = SQLUtil.parseNames(fqn);
                    segments.set(segments.size() -1, tableNm);
                    return segments.stream().collect(Collectors.joining("."));
                }
                ).orElse(normalizedFQN);
    }

    public SourceRef(QueryAnalysis qA,
                     Source inSource,
                     Source source,
                     String alias) {
        this.id = qA.nextId();
        this.inSource = inSource;
        this.source = source;

        if (alias != null && !alias.trim().equals("")) {
            Pair<String, String> p  = Utils.fqNormalizedTableName(source.getSqlEnv(), alias);
            this.alias = Optional.of(p.left);
            fqAlias = Optional.of(buildFQN(source, p.left, p.right));
        } else {
            this.alias = Optional.empty();
            this.fqAlias = Optional.empty();
        }

        ImmutableList.Builder<Column> b = new ImmutableList.Builder();
        ImmutableMap.Builder<String, Column> bm = new ImmutableMap.Builder();

        for(Column c : source.columns()) {
            if (this.alias.isPresent()) {
                String a = this.alias.get();
                SourceRefColumn sc = new SourceRefColumn(this, c, a);
                c = sc;
                bm.put(c.getName(), c);
                bm.put(c.getFQN(), c);
            }
            b.add(c);
        }
        columns = b.build();
        columnMap = bm.build();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Optional<Column> resolveColumn(TObjectName objName) {

        if (!this.alias.isPresent()) {
            return source.resolveColumn(objName);
        }

        List<String> segments = Utils.normalizedColName(source.getSqlEnv(), objName);

        if (segments.size() > 1 && segments.get(segments.size() - 2).equals(alias.get()) ) {
            String nm = segments.stream().collect(Collectors.joining("."));
            return Optional.of(columnMap.get(nm));
        } else {
            return source.resolveColumn(objName);
        }
    }

    @Override
    public TSQLEnv getSqlEnv() {
        return source.getSqlEnv();
    }

    public Source getSource() {
        return source;
    }

    public Optional<String> getAlias() {
        return alias;
    }

    public Optional<String> getFqAlias() {
        return fqAlias;
    }

    public Optional<String> getFQN() {
        return getFqAlias();
    }

    public Iterable<Column>  columns() {
        return columns;
    }

    public Optional<CatalogTable> asCatalogTable() {
        return source.asCatalogTable();
    }

    public Source getInSource() {
        return inSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceRef sourceRef = (SourceRef) o;
        return getId() == sourceRef.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
