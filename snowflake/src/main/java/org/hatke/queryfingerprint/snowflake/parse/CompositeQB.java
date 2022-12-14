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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public ImmutableList<QB> getChildQBs() {
        return childQBs.build();
    }

    public ESetOperatorType getSetOperator() {
        return setOperator;
    }

    private ESetOperatorType setOperator;


    public CompositeQB(QueryAnalysis qA, boolean isTopLevel, QBType qbType, TSelectSqlStatement pTree,
                       Optional<QB> parentQB, Optional<SQLClauseType> parentClause, boolean isCTE) {
        this.qA = qA;
        this.id = qA.nextId();
        this.isTopLevel = isTopLevel;
        this.qbType = qbType;
        this.selectStat = pTree;
        this.parentQB = parentQB;
        this.parentClause = parentClause;
        this.childQBs = new ImmutableList.Builder<>();
        this.setOperator = this.selectStat.getSetOperatorType();
        this.isCTE = isCTE;

        if (parentQB.isPresent()) {
            parentQB.get().addChildQB(this);
        }

        build();
    }

    private void build() {
        TStatementList statements = this.selectStat.getStatements();

        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) instanceof TSelectSqlStatement) {
                TSelectSqlStatement statement = (TSelectSqlStatement) statements.get(i);
                if (!statement.isCombinedQuery()) {
                    new SingleQB(qA, false, QBType.sub_query, statement, Optional.of(this), parentClause, isCTE);
                }
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
    public boolean isCTE() {
        return isCTE;
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
        Stream<QB> cteStream = childQBs.build().stream().flatMap(cqb -> cqb.cteRefs().stream());
        return cteStream.collect(ImmutableList.toImmutableList());
    }

    @Override
    public Optional<SQLClauseType> getParentClause() {
        return this.parentClause;
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
        return Optional.empty();
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
