package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import org.hatke.queryfingerprint.model.QBType;
import org.hatke.queryfingerprint.snowflake.parse.features.CorrelateJoinFeature;

import java.util.Optional;

public interface QB extends Source {

    int getId();

    boolean isTopLevel();

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

    static QB create(QueryAnalysis qA, boolean isTopLevel, QBType qbType, TSelectSqlStatement pTree,
                     Optional<QB> parentQB, Optional<SQLClauseType> parentClause) {
        SupportChecks.supportCheck(pTree,
                parentQB.isPresent() ? parentQB.get().getSelectStat() : pTree,
                s -> s.isCombinedQuery(),
                "Combined queries(union/intersect)"
        );

        return new SingleQB(qA, isTopLevel, qbType, pTree, parentQB, parentClause);
    }

}
