package org.hatke.queryfingerprint.snowflake.parse;

import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

import java.util.Optional;

public interface QB extends Source {

    boolean isTopLevel();

    QBType getQbType();

    TSelectSqlStatement getSelectStat();

    Optional<QB> getParentQB();

    Optional<SQLClauseType> getParentClause();

    Optional<ColumnRef> resolveInputColumn(TObjectName objName);

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
