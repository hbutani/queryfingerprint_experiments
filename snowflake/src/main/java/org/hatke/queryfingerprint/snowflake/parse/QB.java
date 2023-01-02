package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import org.hatke.queryfingerprint.model.QBType;
import org.hatke.queryfingerprint.snowflake.parse.features.CorrelateJoinFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.FuncCallFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.JoinFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.PredicateFeature;

import java.util.Optional;
import java.util.function.Consumer;

public interface QB extends Source {

    int getId();

    boolean isTopLevel();

    boolean isCTE();

    QBType getQbType();

    TSelectSqlStatement getSelectStat();

    Optional<QB> getParentQB();

    void addChildQB(QB child);

    ImmutableList<QB> childQBs();

    ImmutableList<QB> cteRefs();

    Optional<SQLClauseType> getParentClause();

    Optional<ColumnRef> resolveInputColumn(TObjectName objName);

    void addCorrelatedJoinFeature(CorrelateJoinFeature cF);

    default Optional<CatalogTable> asCatalogTable() {
        return Optional.empty();
    }

    void addFeature(Consumer<Features> c);

    static QB create(QueryAnalysis qA, boolean isTopLevel, QBType qbType, TSelectSqlStatement pTree,
                     Optional<QB> parentQB, Optional<SQLClauseType> parentClause, boolean isCTE) {

        if (pTree.isCombinedQuery()) {
            return new CompositeQB(qA, isTopLevel, QBType.composite, pTree, parentQB, parentClause, isCTE);
        } else {
            return new SingleQB(qA, isTopLevel, qbType, pTree, parentQB, parentClause, isCTE);
        }

    }

    class Features {

        ImmutableList.Builder<Column> accesedColumns = new ImmutableList.Builder<>();
        ImmutableList.Builder<FuncCallFeature> functionApplications = new ImmutableList.Builder<>();

        ImmutableList.Builder<Column> filteredColumns = new ImmutableList.Builder<>();

        /**
         * execution plan for this predicate can be potentially influenced by the physical layout of the table.
         */
        ImmutableList.Builder<PredicateFeature> prunablePredicates = new ImmutableList.Builder<>();

        ImmutableList.Builder<PredicateFeature> otherPredicates = new ImmutableList.Builder<>();

        ImmutableList.Builder<JoinFeature> joins = new ImmutableList.Builder<>();

        ImmutableList.Builder<CorrelateJoinFeature> correlateJoins = new ImmutableList.Builder<>();

        ImmutableList.Builder<Column> columnsFromParent = new ImmutableList.Builder<>();
    }

}
