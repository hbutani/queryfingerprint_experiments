package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.ESetOperatorType;
import gudusoft.gsqlparser.TStatementList;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import org.hatke.queryfingerprint.model.QBType;
import org.hatke.queryfingerprint.snowflake.parse.features.CorrelateJoinFeature;

import java.util.Optional;
import java.util.function.Consumer;

public class CompositeQB implements QB {

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

    public ImmutableList<QB> getChildQBs() {
        return childQBs.build();
    }

    public ESetOperatorType getSetOperator() {
        return setOperator;
    }

    private ESetOperatorType setOperator;


    public CompositeQB(QueryAnalysis qA, boolean isTopLevel, QBType qbType, TSelectSqlStatement pTree,
                       Optional<QB> parentQB, Optional<SQLClauseType> parentClause) {
        this.qA = qA;
        this.id = qA.nextId();
        this.isTopLevel = isTopLevel;
        this.qbType = qbType;
        this.selectStat = pTree;
        this.parentQB = parentQB;
        this.parentClause = parentClause;
        this.childQBs = new ImmutableList.Builder<>();
        this.setOperator = this.selectStat.getSetOperatorType();

        if (parentQB.isPresent()) {
            parentQB.get().addChildQB(this);
        }

        build();
    }

    private void build() {
        TStatementList statements = this.selectStat.getStatements();

        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) instanceof TSelectSqlStatement) {
                new SingleQB(qA, false, QBType.regular, (TSelectSqlStatement) statements.get(i), Optional.of(this), parentClause);
            }
        }

    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isTopLevel() {
        return isTopLevel;
    }

    @Override
    public QBType getQbType() {
        return qbType;
    }

    @Override
    public TSelectSqlStatement getSelectStat() {
        return selectStat;
    }

    @Override
    public Optional<QB> getParentQB() {
        return parentQB;
    }

    @Override
    public void addChildQB(QB child) {
        childQBs.add(child);
    }

    @Override
    public ImmutableList<QB> childQBs() {
        return childQBs.build();
    }

    @Override
    public ImmutableList<QB> cteRefs() {
        return childQBs.build().get(0).cteRefs();
    }

    @Override
    public Optional<SQLClauseType> getParentClause() {
        return Optional.empty();
    }

    @Override
    public Optional<ColumnRef> resolveInputColumn(TObjectName objName) {
        return Optional.empty();
    }

    @Override
    public void addCorrelatedJoinFeature(CorrelateJoinFeature cF) {

    }

    @Override
    public Optional<CatalogTable> asCatalogTable() {
        return QB.super.asCatalogTable();
    }

    @Override
    public void addFeature(Consumer<Features> c) {

    }

    @Override
    public Optional<Column> resolveColumn(TObjectName objName) {
        return childQBs().get(0).resolveColumn(objName);
    }

    @Override
    public TSQLEnv getSqlEnv() {
        return qA.getSqlEnv();
    }

    @Override
    public Iterable<Column> columns() {
        return childQBs().get(0).columns();
    }
}
