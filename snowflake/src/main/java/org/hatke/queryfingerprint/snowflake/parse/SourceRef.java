package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
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

    public SourceRef(QueryAnalysis qA,
                     Source inSource,
                     Source source, String alias) {
        this.id = qA.nextId();
        this.inSource = inSource;
        this.source = source;

        if (alias != null && !alias.trim().equals("")) {
            Pair<String, String> p  = Utils.fqNormalizedTableName(source.getSqlEnv(), alias);
            this.alias = Optional.of(p.left);
            fqAlias = Optional.of(p.right);
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
                bm.put(sc.getSourceColumn().getFQN(), c);
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

    public Iterable<Column>  columns() {
        return columns;
    }

    public Source getInSource() {
        return inSource;
    }
}
