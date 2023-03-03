package org.hatke.queryfingerprint.snowflake.parse;


import com.google.common.base.Objects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.EJoinType;
import gudusoft.gsqlparser.nodes.*;
import gudusoft.gsqlparser.sqlenv.ESQLDataObjectType;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import org.hatke.queryfingerprint.model.JoinType;
import org.hatke.queryfingerprint.model.QBType;
import org.hatke.queryfingerprint.snowflake.parse.features.CorrelateJoinFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.ExprFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.ExprKind;
import org.hatke.queryfingerprint.snowflake.parse.features.FuncCallFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.JoinFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.PredicateFeature;
import org.hatke.utils.Pair;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SingleQB implements QB {

    static final Logger LOGGER = Utils.getLogger();

    private final QueryAnalysis qA;

    /**
     * a unique identifier within the user query.
     */
    private int id;

    /**
     * is this the overall user query
     */
    private boolean isTopLevel;

    private boolean isCTE;

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
             Optional<QB> parentQB, Optional<SQLClauseType> parentClause, boolean isCTE) {


        this.qA = qA;
        this.id = qA.nextId();
        this.isTopLevel = isTopLevel;
        this.qbType = qbType;
        this.selectStat = pTree;
        this.parentQB = parentQB;
        this.parentClause = parentClause;
        this.childQBs = new ImmutableList.Builder<>();
        this.isCTE = isCTE;

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
        analyzeSourceJoins();
        analyzeWhereClause();
        analyzeGroupByClause();
        analyzeOrderByClause();
        analyzeResultClause();
    }

    private void analyzeSourceJoins() {
        TJoinList joins = selectStat.getJoins();
        joins.forEach(j -> {
            TJoinItemList joinItems = j.getJoinItems();
            joinItems.forEach(ji -> {
                TExpression onCondition = ji.getOnCondition();
                extractWhereOrOnclause(onCondition, Optional.of(JoinType.valueOf(ji.getJoinType().name())));
            });
        });
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
                        Optional.empty(), Optional.empty(), true);
                SourceRef srcRef = new SourceRef(qA, this, src, cteName.toString());
                qA.addCTE(srcRef);
            }
        }

    }

    private void buildSources() {
        TTableList fromTables = selectStat.tables;
        ImmutableList.Builder<Source> b = new ImmutableList.Builder();

        if (fromTables != null && fromTables.size() > 0) {
            for (int i = 0; i < fromTables.size(); ++i) {
                TTable table = fromTables.getTable(i);
                SupportChecks.tableChecks(table, selectStat);
                Source src = null;

                if (table.getCTE() != null) {

                    Pair<String, String> cteNm =
                            Utils.fqNormalizedTableName(getSqlEnv(), table.getCTE().getTableName());

                    src = qA.getCTE(cteNm.right);

                    if (src == null) {
                        throw new IllegalStateException(
                                String.format("Unable resolve CTE reference %$1s\n" +
                                                "Clause: %2$s\n",
                                        cteNm.left, table.getCTE())
                        );
                    }
                } else if (table.getSubquery() != null) {
                    src = QB.create(qA, false, QBType.sub_query, table.getSubquery(),
                            Optional.of(this), Optional.of(SQLClauseType.from), isCTE);
                } else {
                    src = new CatalogTable(qA, table);
                }

                if (table.getAliasName() != null) {
                    src = new SourceRef(qA, this, src, table.getAliasName());
                }

                /*
                 * Immediately add the columns of this source to the SourceCoMap
                 * so subsequent query blocks in the from caluse can refer to
                 * columns from this source.
                 *
                 * See sub-query8.sql as an example. Example maybe not
                 * valid in some flavors of sql; but this kind of column
                 * resolution is required for lateral_joins, which is
                 * supported by most SQL Flavors.
                 */
                setupInputResolution(src);
                b.add(src);
            }
        }

        fromSources = b.build();
    }

    /**
     * across all input Sources mapping from col(name, fqn) to Column, provided it is unambiguous.
     */
    ImmutableMap<String, Column> unambiguousSourceColMap = ImmutableMap.of();

    private void setupInputResolution(Source s) {

        Map<String, Column> sourceColumnMap = new HashMap<>(unambiguousSourceColMap);
        Set<String> ambiguousColNames = new HashSet<>();

        Function<Column, Void> addCol2 = col -> {

            String nm = col.getName();

            sourceColumnMap.put(col.getFQN(), col);

            if (sourceColumnMap.containsKey(nm)) {
                ambiguousColNames.add(nm);
            } else {
                sourceColumnMap.put(nm, col);
            }

            return null;
        };

        for (Column c : s.columns()) {
            addCol2.apply(c);
        }

        for (String amCol : ambiguousColNames) {
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

        if (c == null && parentQB.isPresent()) {
            c = parentQB.get().resolveInputColumn(objName).orElse(null);
            if (c instanceof Column) {
                c = new Column.CorrelateColRef((Column) c, parentQB.get());
            }
        }

        return Optional.ofNullable(c);
    }

    ImmutableMap<String, Column> getUnambiguousSourceColMap() {
        return unambiguousSourceColMap;
    }

    public void addFeature(Consumer<Features> c) {
        c.accept(blockFeatures);
    }

    private void addExprFeature(ExprFeature eInfo, boolean isWhereConjunct) {
        addExprFeature(eInfo, isWhereConjunct, Optional.empty());
    }

    private void addExprFeature(ExprFeature eInfo,
                                boolean isWhereConjunct, Optional<JoinType> joinType) {
        eInfo.getColumnRefs().stream().forEach(crf -> {
                    if (!crf.isCorrelated()) {
                        blockFeatures.accesedColumns.add(crf.getColumn());
                    } else {
                        blockFeatures.columnsFromParent.add(crf.getColumn());
                        parentQB.get().addFeature(blockFeatures -> blockFeatures.accesedColumns.add(crf.getColumn()));
                    }
                }
        );
        eInfo.getFuncCalls().stream().forEach(fcf -> blockFeatures.functionApplications.add(fcf));
        eInfo.getPredicate().stream().forEach(p -> {

            p.getColumnRefs().stream().forEach(crf -> blockFeatures.filteredColumns.add(crf.getColumn()));

            if (isWhereConjunct) {
                blockFeatures.prunablePredicates.add(p);
            } else {
                blockFeatures.otherPredicates.add(p);
            }
        });

        if (eInfo.getExprKind() == ExprKind.join) {
            JoinFeature jf = (JoinFeature) eInfo;
            if (joinType.isPresent()) {
                jf = new JoinFeature(jf.getTExpression(), jf.getLeftFeature(), jf.getRightFeature(), joinType.get());
            }
            blockFeatures.joins.add(jf);
        }

        if (eInfo.getExprKind() == ExprKind.correlate_join) {
            parentQB.get().addCorrelatedJoinFeature((CorrelateJoinFeature) eInfo);
        }
    }

    private void addGroupByFeature(ExprFeature eInfo) {
        blockFeatures.groupedColumns.add(eInfo);
    }

    private void addOrderByFeature(ExprFeature eInfo) {
        blockFeatures.orderedColumns.add(eInfo);
    }

    private ImmutableMap.Builder<TExpression, QB> whereSubQBs = new ImmutableMap.Builder<>();


    public ImmutableCollection<QB> getWhereSubQueryBlocks() {
        return whereSubQBs.build().values();
    }

    private void buildSubqueriesInWhere(TExpression whereCond) {
        Set<TExpression> subqueries = FindExpressionsByType.findSubqueries(whereCond);

//        LOGGER.info(TreeDump.dump(whereCond));

        for (TExpression subE : subqueries) {
            QB subQB = QB.create(qA, false, QBType.sub_query, subE.getSubQuery(),
                    Optional.of(this), Optional.of(SQLClauseType.where), isCTE);
            whereSubQBs.put(subE, subQB);
        }
    }

    private void analyzeWhereClause() {

        if (selectStat.getWhereClause() == null || selectStat.getWhereClause().getCondition() == null) {
            return;
        }

        TExpression whereCond = selectStat.getWhereClause().getCondition();
        extractWhereOrOnclause(whereCond, Optional.empty());

    }

    private void extractWhereOrOnclause(TExpression whereCond, Optional<JoinType> joinType) {
        if (whereCond == null) return;

        buildSubqueriesInWhere(whereCond);

        ArrayList conjuncts = null;
        if (whereCond.getExpressionType() == EExpressionType.logical_and_t) {
            conjuncts = whereCond.getFlattedAndOrExprs();
        } else {
            conjuncts = new ArrayList(Arrays.asList(whereCond));
        }

        for (Object o : conjuncts) {
            TExpression expr = (TExpression) o;

            if (expr.getExpressionType() == EExpressionType.parenthesis_t) {
                expr = expr.getLeftOperand();
            }

            ExpressionAnalyzer eA =
                    new ExpressionAnalyzer(this, expr);
            Pair<ExprKind, ImmutableList<ExprFeature>> exprInfos =
                    eA.analyze();
            if (exprInfos.left != ExprKind.composite) {
                addExprFeature(exprInfos.right.get(0), true, joinType);
            } else {
                exprInfos.right.forEach(eI -> addExprFeature(eI, false, joinType));
            }
        }
    }

    private void analyzeGroupByExpr(TExpression gByExpr) {
        ExprFeature groupbyFeature = ExprFeature.match(gByExpr, this);
        if (groupbyFeature != null) {
            addExprFeature(groupbyFeature, false);
            addGroupByFeature(groupbyFeature);
        }
    }

    private void analyzeGroupByClause() {
        if (selectStat.getGroupByClause() != null) {
            TGroupByItemList items = selectStat.getGroupByClause().getItems();
            for (int i = 0; i < items.size(); i++) {
                TGroupByItem item = items.getGroupByItem(i);

                if (item.getRollupCube() != null) {
                    TExpressionList eList = item.getRollupCube().getItems();
                    for (TExpression expr : eList) {
                        analyzeGroupByExpr(expr);
                    }
                } else {
                    analyzeGroupByExpr(item.getExpr());
                }
            }
        }

        // for each group expr run the ExpressionAnalyzer
        // add to grouped Columns
        // add to functionApplications
    }

    private void analyzeOrderByClause() {
        if (selectStat.getOrderbyClause() != null) {
            TOrderByItemList items = selectStat.getOrderbyClause().getItems();
            for (int i = 0; i < items.size(); i++) {
                TOrderByItem item = items.getOrderByItem(i);
                ExprFeature orderByFeature = ExprFeature.match(item.getSortKey(), this);
                if (orderByFeature != null) {
                    addExprFeature(orderByFeature, false);
                    addOrderByFeature(orderByFeature);
                }
            }
        }
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
                    alias == null ? resExpr.toString() : alias,
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

        for (TResultColumn r : selectStat.getResultColumnList()) {
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

        for (Source src : getFromSources()) {
            if (src instanceof SourceRef) {
                SourceRef srcRef = (SourceRef) src;
                Source referencedSource = srcRef.getSource();

                while (referencedSource instanceof SourceRef) {
                    referencedSource = ((SourceRef) referencedSource).getSource();
                }

                if (referencedSource instanceof QB && ((QB) referencedSource).getQbType() == QBType.cte) {
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

    @Override
    public boolean isCTE() {
        return isCTE;
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

    public ImmutableSet<Column> getGroupedColumns() {
        return blockFeatures.groupedColumns.build().stream()
                .flatMap(c -> c.getColumnRefs().stream().map(cr -> cr.getColumn()))
                .collect(ImmutableSet.toImmutableSet());
    }

    public ImmutableSet<Column> getOrderedColumns() {
        return blockFeatures.orderedColumns.build().stream()
                .flatMap(c -> c.getColumnRefs().stream().map(cr -> cr.getColumn()))
                .collect(ImmutableSet.toImmutableSet());
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

    public ImmutableList<Column> columnsFromParent() {
        return blockFeatures.columnsFromParent.build();
    }

    public void addCorrelatedJoinFeature(CorrelateJoinFeature cF) {
        assert (cF.getColRef().appearsInQB().get() == this);
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
}
