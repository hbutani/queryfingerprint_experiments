package org.hatke.queryfingerprint.model;

import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.stream.Collectors;

public class Join implements Serializable  {
    private static final long serialVersionUID = 1771503490575888160L;

    private final String leftTable;
    private final String rightTable;
    private final ImmutableList<Condition> joinConditions;
    private final JoinType type;

    public Join(String leftTable, String rightTable, ImmutableList<Condition> joinConditions, JoinType type) {

        if (leftTable.compareTo(rightTable) > 0 && type.isFlippable()) {
            this.leftTable = leftTable;
            this.rightTable = rightTable;
            this.joinConditions = ImmutableList.copyOf(
                            joinConditions.stream().map(c -> c.flip()).collect(Collectors.toList())
            );
            this.type = type;
        } else {
            this.leftTable = leftTable;
            this.rightTable = rightTable;
            this.joinConditions = joinConditions;
            this.type = type;
        }
    }

    public String getLeftTable() {
        return leftTable;
    }

    public String getRightTable() {
        return rightTable;
    }

    public ImmutableList<Condition> getJoinConditions() {
        return joinConditions;
    }

    public JoinType getType() {
        return type;
    }

    public static final class Condition implements Serializable {
        private static final long serialVersionUID = 1382946828217636364L;

        private final String leftColumn;
        private final String rightColumn;

        public Condition(String left, String right) {
            this.leftColumn = left;
            this.rightColumn = right;
        }

        public String getLeftColumn() {
            return leftColumn;
        }

        public String getRightColumn() {
            return rightColumn;
        }

        Condition flip() {
            return new Condition(rightColumn, leftColumn);
        }
    }

}
