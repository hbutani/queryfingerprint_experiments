package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.base.Predicate;
import gudusoft.gsqlparser.nodes.TParseTreeNode;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

public class SupportChecks {

    static <T extends TParseTreeNode> void supportCheck(
            TSelectSqlStatement selectStat,
            T checkNode,
            Predicate<T> p,
            String feature) {
        if (p.apply(checkNode)) {
            throw new UnsupportedOperationException(
                    String.format("Currently %1$s is not supported.\n" +
                            "Clause: %2$s\n" +
                            "SQL: %3$s\n",
                            feature,
                            selectStat.toString(),
                            checkNode.toString())
            );
        }
    }

    static void tableChecks(TTable tab,
                            TSelectSqlStatement selectStat) {
        supportCheck(selectStat, tab,
                table -> table.getLateralViewList() != null && !table.getLateralViewList().isEmpty(),
                "lateral views"
        );

        supportCheck(selectStat, tab,
                table -> table.getFuncCall() != null,
                "table functions"
        );

        supportCheck(selectStat, tab,
                table -> table.getTableExpr() != null,
                "table expressions"
        );
    }

}
