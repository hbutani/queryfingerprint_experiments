package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gudusoft.gsqlparser.nodes.TCTE;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.nodes.TTableList;
import gudusoft.gsqlparser.sqlenv.ESQLDataObjectType;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import org.hatke.utils.Pair;

import java.util.Optional;

class SingleQB implements Source, QB {

    private final QueryAnalysis qA;

    /**
     * a unique identifier within the user query.
     */
    private int id;

    /**
     * is this the overall user query
     */
    private boolean isTopLevel;

    private QBType qbType;

    /**
     * parse tree of this Query Block.
     */
    private TSelectSqlStatement selectStat;

    private Optional<QB> parentQB;
    private Optional<SQLClauseType> parentClause;

    private ImmutableList<Source> fromSources;

    private ImmutableList<Column> columns;
    private ImmutableMap<String, Column> columnsMap;

    private ImmutableList<Column> projectedColumns;
    private ImmutableList<FunctionApplication> functionApplications;

    private ImmutableList<Column> filteredColumns;
    private ImmutableList<Predicate> columnFilters;

    private ImmutableList<Join> joins;


    SingleQB(QueryAnalysis qA, boolean isTopLevel, QBType qbType, TSelectSqlStatement pTree,
              Optional<QB> parentQB, Optional<SQLClauseType> parentClause) {

        SupportChecks.supportCheck(pTree,
                qA.getTopLevelQB() != null ? qA.getTopLevelQB().getSelectStat() : pTree,
                s -> s.isCombinedQuery(),
                "Combined queries(union/intersect)"
                );

        this.qA = qA;
        this.id = qA.nextId();
        this.isTopLevel = isTopLevel;
        this.qbType = qbType;
        this.selectStat = pTree;
        this.parentQB = parentQB;
        this.parentClause = parentClause;

        build();
    }

    @Override
    public Optional<Column> resolveColumn(TObjectName objName) {
        return Optional.empty();
    }

    @Override
    public TSQLEnv getSqlEnv() {
        return qA.getSqlEnv();
    }

    public Iterable<Column> columns() {
        return getColumns();
    }

    private void build() {
        buildCTEs();
        buildSources();
        setupInputResolution();
        analyzeWhereClause();
        analyzeGroupByClause();
        analyzeResultClause();
    }

    private void buildCTEs() {
        ImmutableMap.Builder<String, Source> bm = new ImmutableMap.Builder();
        if (selectStat.getCteList() != null) {

            SupportChecks.supportCheck(selectStat,
                    selectStat.getCteList(),
                    n -> !isTopLevel,
                    "CTEs only supported at the top-level"
            );

            for (TCTE cte : selectStat.getCteList()) {
                TSelectSqlStatement cteSelect = cte.getSubquery();
                TObjectName cteName = cte.getTableName();
                Source src = QB.create(qA, false, QBType.cte, cteSelect,
                        Optional.empty(), Optional.empty());
                SourceRef srcRef = new SourceRef(qA, this, src, cteName.toString());
                bm.put(srcRef.alias.get(), srcRef);
                bm.put(srcRef.fqAlias.get(), srcRef);
            }
            qA.setCTEMap(bm.build());
        }

    }

    private void buildSources() {
        TTableList fromTables = selectStat.tables;
        ImmutableList.Builder<Source> b = new ImmutableList.Builder();

        if (fromTables != null && fromTables.size() > 0) {
            for(int i = 0; i < fromTables.size(); ++i) {
                TTable table = fromTables.getTable(i);
                SupportChecks.tableChecks(table, selectStat);
                Source src = null;

                if (table.getCTE() != null) {

                    Pair<String, String> cteNm =
                            Utils.fqNormalizedTableName(getSqlEnv(), table.getCTE().getTableName());

                    src = qA.getCTE(cteNm.right);

                    if ( src == null) {
                        throw new IllegalStateException(
                                String.format("Unable resolve CTE reference %$1s\n" +
                                        "Clause: %2$s\n",
                                        cteNm.left, table.getCTE())
                        );
                    }
                } else if (table.getSubquery() != null) {
                    src = QB.create(qA, false, QBType.sub_query, table.getSubquery(),
                            Optional.of(this), Optional.of(SQLClauseType.from));
                } else {
                    src = new CatalogTable(qA, table);
                }

                if (table.getAliasName() != null) {
                    src = new SourceRef(qA, this, src, table.getAliasName());
                }
                b.add(src);
            }
        }

        fromSources = b.build();
    }

    /**
     * provide mapping from a table(name, fqn) and subquery(alias, fqAlias) to the Source object.
     */
    ImmutableMap<String, Source> sourceAliasMap;

    /**
     * across all input Sources mapping from col(name, fqn) to Column, provided it is unambiguous.
     */
    ImmutableMap<String, Column> unambiguousSourceColMap;

    private void setupInputResolution() {
        // TODO
        // setup sourceAliasMap, unambiguousSourceColMap
    }

    public Optional<ColumnRef> resolveInputColumn(TObjectName objName) {
        Pair<String, String> cName =
                Utils.normalizedName(getSqlEnv(), objName, ESQLDataObjectType.dotColumn);
        ColumnRef c = unambiguousSourceColMap.get(cName.left);

        if (c == null) {
            c = unambiguousSourceColMap.get(cName.right);
        }

        if ( c == null && parentQB.isPresent() ) {
            c =  parentQB.get().resolveInputColumn(objName).orElse(null);
        }

        return Optional.ofNullable(c);
    }


    private void analyzeWhereClause() {
        // TODO
        // for each conjunct run the ExpressionAnalyzer
        // add to extracted predicates and joins of this QB
        // add to functionApplications
    }

    private void analyzeGroupByClause() {
        // TODO
        // for each group expr run the ExpressionAnalyzer
        // add to grouped Columns
        // add to functionApplications
    }

    private void analyzeResultClause() {
        // TODO
        // for each resultColumn run the ExpressionAnalyzer
        // add to extracted predicates and joins of this QB
        // add to functionApplications

        // setup the output Shape used by resolveColumn
    }

    ImmutableList<Source> getFromSources() {
        return fromSources;
    }

    public int getId() {
        return id;
    }

    public boolean isTopLevel() {
        return isTopLevel;
    }

    public QBType getQbType() {
        return qbType;
    }

    public TSelectSqlStatement getSelectStat() {
        return selectStat;
    }

    public Optional<QB> getParentQB() {
        return parentQB;
    }

    public Optional<SQLClauseType> getParentClause() {
        return parentClause;
    }

    public ImmutableList<Column> getColumns() {
        // TODO fixme
        return ImmutableList.of();
    }

    public ImmutableMap<String, Column> getColumnsMap() {
        return columnsMap;
    }

    public ImmutableList<Column> getProjectedColumns() {
        return projectedColumns;
    }

    public ImmutableList<FunctionApplication> getFunctionApplications() {
        return functionApplications;
    }

    public ImmutableList<Column> getFilteredColumns() {
        return filteredColumns;
    }

    public ImmutableList<Predicate> getColumnFilters() {
        return columnFilters;
    }

    public ImmutableList<Join> getJoins() {
        return joins;
    }
}
