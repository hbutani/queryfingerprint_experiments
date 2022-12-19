package org.hatke.queryfingerprint.snowflake.parse;


import com.google.common.base.Objects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.nodes.TCTE;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TResultColumn;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.nodes.TTableList;
import gudusoft.gsqlparser.sqlenv.ESQLDataObjectType;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import org.hatke.queryfingerprint.model.QBType;
import org.hatke.queryfingerprint.snowflake.parse.features.CorrelateJoinFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.ExprFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.ExprKind;
import org.hatke.queryfingerprint.snowflake.parse.features.FuncCallFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.JoinFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.PredicateFeature;
import org.hatke.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

class SingleQB implements QB {

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

    private ImmutableList.Builder<QB> childQBs;

    private ImmutableList<Source> fromSources;

    private final Features blockFeatures = new Features();

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
        this.childQBs = new ImmutableList.Builder<>();

        if (parentQB.isPresent()) {
            parentQB.get().addChildQB(this);
        }

        build();
    }

    @Override
    public Optional<Column> resolveColumn(TObjectName objName) {
        String colName = Utils.stringValue(objName, ESQLDataObjectType.dotColumn);
        return Optional.ofNullable(outColumnsMap.get(colName));
    }

    @Override
    public TSQLEnv getSqlEnv() {
        return qA.getSqlEnv();
    }

    public Iterable<Column> columns() {
        return getColumns();
    }

    public void addChildQB(QB child) {
        childQBs.add(child);
    }

    public ImmutableList<QB> childQBs() {
        return childQBs.build();
    }

    private void build() {
        buildCTEs();
        buildSources();
        // TODO analyzeSourceJoins();
        setupInputResolution();
        analyzeWhereClause();
        analyzeGroupByClause();
        analyzeResultClause();
    }

    private void buildCTEs() {

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
                qA.addCTE(srcRef);
            }
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
     * across all input Sources mapping from col(name, fqn) to Column, provided it is unambiguous.
     */
    ImmutableMap<String, Column> unambiguousSourceColMap;

    private void setupInputResolution() {

        Map<String, Column> sourceColumnMap = new HashMap<>();
        Set<String> ambiguousColNames = new HashSet<>();

        Function<Column, Void>  addCol2 = col -> {

            String nm = col.getName();

            sourceColumnMap.put(col.getFQN(), col);

            if (sourceColumnMap.containsKey(nm)) {
                ambiguousColNames.add(nm);
            } else {
                sourceColumnMap.put(nm, col);
            }

            return null;
        };


        for(Source s : fromSources) {
            for(Column c : s.columns()) {
                addCol2.apply(c);
            }
        }

        for(String amCol : ambiguousColNames) {
            sourceColumnMap.remove(amCol);
        }

        unambiguousSourceColMap = ImmutableMap.copyOf(sourceColumnMap);

    }

    public Optional<ColumnRef> resolveInputColumn(TObjectName objName) {
        Pair<String, String> cName = Utils.fqNormalizedColName(getSqlEnv(), objName);
        ColumnRef c = unambiguousSourceColMap.get(cName.left);

        if (c == null) {
            c = unambiguousSourceColMap.get(cName.right);
        }

        if ( c == null && parentQB.isPresent() ) {
            c =  parentQB.get().resolveInputColumn(objName).orElse(null);
            if ( c instanceof Column) {
                c = new Column.CorrelateColRef((Column) c, parentQB.get());
            }
        }

        return Optional.ofNullable(c);
    }

    ImmutableMap<String, Column> getUnambiguousSourceColMap() {
        return unambiguousSourceColMap;
    }

    private void addExprFeature(ExprFeature eInfo,
                                boolean isWhereConjunct) {
        eInfo.getColumnRefs().stream().forEach(crf -> blockFeatures.accesedColumns.add(crf.getColumn()));
        eInfo.getFuncCalls().stream().forEach(fcf -> blockFeatures.functionApplications.add(fcf));
        eInfo.getPredicate().stream().forEach(p -> {

            p.getColumnRefs().stream().map(crf -> blockFeatures.filteredColumns.add(crf.getColumn()));

            if (isWhereConjunct) {
                blockFeatures.prunablePredicates.add(p);
            } else {
                blockFeatures.otherPredicates.add(p);
            }
        });

        if (eInfo.getExprKind() == ExprKind.join) {
            blockFeatures.joins.add((JoinFeature) eInfo);
        }

        if (eInfo.getExprKind() == ExprKind.correlate_join) {
            parentQB.get().addCorrelatedJoinFeature((CorrelateJoinFeature) eInfo);
        }
    }

    private ImmutableMap.Builder<TExpression, QB> whereSubQBs = new ImmutableMap.Builder<>();


    public ImmutableCollection<QB> getWhereSubQueryBlocks() {
        return whereSubQBs.build().values();
    }

    private void buildSubqueriesInWhere(TExpression whereCond) {
        ImmutableSet<TExpression> subqueries = FindExpressionsByType.findSubqueries(whereCond);
        for(TExpression subE : subqueries) {
            QB subQB = QB.create(qA, false, QBType.sub_query, subE.getSubQuery(),
                    Optional.of(this), Optional.of(SQLClauseType.where));
            whereSubQBs.put(subE, subQB);
        }
    }

    private void analyzeWhereClause() {

        if (selectStat.getWhereClause() == null || selectStat.getWhereClause().getCondition() == null) {
            return;
        }

        TExpression whereCond = selectStat.getWhereClause().getCondition();
        buildSubqueriesInWhere(whereCond);

        ArrayList conjuncts = null;
        if (whereCond.getExpressionType() == EExpressionType.logical_and_t) {
            conjuncts = whereCond.getFlattedAndOrExprs();
        } else {
            conjuncts = new ArrayList(Arrays.asList(whereCond));
        }

        for(Object o : conjuncts) {
            TExpression expr = (TExpression) o;

            if (expr.getExpressionType() == EExpressionType.parenthesis_t) {
                expr = expr.getLeftOperand();
            }

            ExpressionAnalyzer eA =
                    new ExpressionAnalyzer(this, expr);
            Pair<ExprKind, ImmutableList<ExprFeature>> exprInfos =
                    eA.analyze();
            if (exprInfos.left != ExprKind.composite) {
                addExprFeature(exprInfos.right.get(0), true);
            } else {
                exprInfos.right.forEach(eI -> addExprFeature(eI, false));
            }
        }

    }

    private void analyzeGroupByClause() {
        // TODO
        // for each group expr run the ExpressionAnalyzer
        // add to grouped Columns
        // add to functionApplications
    }

    private ImmutableList<Column> outColumns = ImmutableList.of();
    private ImmutableMap<String, Column> outColumnsMap = ImmutableMap.of();

    private QBOutColumn analyzeResultColumn(TResultColumn resultColumn) {
        TExpression resExpr = resultColumn.getExpr();
        String alias = resultColumn.getColumnAlias();
        alias = alias.trim().equals("") ? null : alias;

        if (alias != null) {
            alias = Utils.normalizedColName(getSqlEnv(), resultColumn.getAliasClause().getAliasName()).get(0);
        }

        ExpressionAnalyzer eA = new ExpressionAnalyzer(this, resExpr);
        Pair<ExprKind, ImmutableList<ExprFeature>> exprInfos = eA.analyze();

        exprInfos.right.stream().forEach(eI -> addExprFeature(eI, false));

        if (exprInfos.left == ExprKind.column_ref) {
            ExprFeature eI = exprInfos.right.get(0);
            Column col = eI.getColumnRefs().get(0).getColumn();
            return new QBOutColumn(this, qA.nextId(),
                    alias == null ? col.getName() : alias,
                    ImmutableList.of(),
                    Optional.of(col));
        } else {
            List<Column> dependColumns =
                    exprInfos.right.stream().
                            flatMap(eI -> eI.getColumnRefs().stream().map(cr -> cr.getColumn()))
                    .collect(Collectors.toList());

            return new QBOutColumn(this, qA.nextId(),
                    alias == null ? resExpr.toString() : alias ,
                    ImmutableList.copyOf(dependColumns),
                    Optional.empty());
        }
    }

    private void analyzeResultClause() {

        if (selectStat.getResultColumnList() == null || selectStat.getResultColumnList().size() == 0) {
            return;
        }

        ImmutableList.Builder<Column> outC = new ImmutableList.Builder<>();
        ImmutableMap.Builder<String, Column> outM = new ImmutableMap.Builder<>();

        for(TResultColumn r : selectStat.getResultColumnList()) {
            QBOutColumn oCol = analyzeResultColumn(r);
            outC.add(oCol);
            outM.put(oCol.getName(), oCol);
        }

        outColumns = outC.build();
        outColumnsMap = outM.build();
    }

    ImmutableList<Source> getFromSources() {
        return fromSources;
    }

    public ImmutableList<QB> cteRefs() {
        ImmutableList.Builder<QB> b = new ImmutableList.Builder<>();

        for(Source src : getFromSources()) {
            if (src instanceof SourceRef) {
                SourceRef srcRef = (SourceRef) src;
                Source referencedSource = srcRef.getSource();

                while(referencedSource instanceof SourceRef) {
                    referencedSource = ((SourceRef)referencedSource).getSource();
                }

                if (referencedSource instanceof QB && ((QB)referencedSource).getQbType() == QBType.cte) {
                    b.add((QB) referencedSource);
                }
            }
        }
        return b.build();
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
        return outColumns;
    }

    public ImmutableMap<String, Column> getColumnsMap() {
        return outColumnsMap;
    }

    public ImmutableSet<Column> getScannedColumns() {
        ImmutableList.Builder<Column> b = new ImmutableList.Builder<>();
        b.addAll(blockFeatures.accesedColumns.build());
        b.addAll(blockFeatures.filteredColumns.build());
        return b.build().stream().collect(ImmutableSet.toImmutableSet());
    }

    public ImmutableSet<Column> getFilteredColumns() {
        return blockFeatures.filteredColumns.build().stream().collect(ImmutableSet.toImmutableSet());
    }

    public ImmutableList<PredicateFeature> prunablePredicates() {
        return blockFeatures.prunablePredicates.build();
    }

    public ImmutableList<PredicateFeature> otherPredicates() {
        return blockFeatures.otherPredicates.build();
    }

    public ImmutableList<FuncCallFeature> functionApplications() {
        return blockFeatures.functionApplications.build();
    }

    public ImmutableList<JoinFeature> joins() {
        return blockFeatures.joins.build();
    }

    public ImmutableList<CorrelateJoinFeature> correlatedJoins() {
        return blockFeatures.correlateJoins.build();
    }

    public void addCorrelatedJoinFeature(CorrelateJoinFeature cF) {
        assert(cF.getColRef().appearsInQB().get() == this);
        blockFeatures.correlateJoins.add(cF);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleQB singleQB = (SingleQB) o;
        return getId() == singleQB.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    class Features {

        private ImmutableList.Builder<Column> accesedColumns = new ImmutableList.Builder<>();
        private ImmutableList.Builder<FuncCallFeature> functionApplications = new ImmutableList.Builder<>();

        private ImmutableList.Builder<Column> filteredColumns = new ImmutableList.Builder<>();

        /**
         * execution plan for this predicate can be potentially influenced by the physical layout of the table.
         */
        private ImmutableList.Builder<PredicateFeature> prunablePredicates = new ImmutableList.Builder<>();

        private ImmutableList.Builder<PredicateFeature> otherPredicates = new ImmutableList.Builder<>();

        private ImmutableList.Builder<JoinFeature> joins = new ImmutableList.Builder<>();

        private ImmutableList.Builder<CorrelateJoinFeature> correlateJoins = new ImmutableList.Builder<>();
    }
}
