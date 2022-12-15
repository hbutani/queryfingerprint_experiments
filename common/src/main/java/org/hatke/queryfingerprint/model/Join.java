package org.hatke.queryfingerprint.model;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.stream.Collectors;

public class Join implements Serializable  {
    private static final long serialVersionUID = 1771503490575888160L;

    private final String leftTable;
    private final String rightTable;

    private final String leftColumn;

    private final String rightColumn;
    private final JoinType type;

    public Join(String leftTable, String rightTable, String leftColumn, String rightColumn, JoinType type) {

        if (leftTable.compareTo(rightTable) > 0 && type.isFlippable()) {
            this.leftTable = rightTable;
            this.rightTable = leftTable;
            this.leftColumn = rightColumn;
            this.rightColumn = leftColumn;
            this.type = type;
        } else {
            this.leftTable = leftTable;
            this.rightTable = rightTable;
            this.leftColumn = leftColumn;
            this.rightColumn = rightColumn;
            this.type = type;
        }
    }

    public String getLeftTable() {
        return leftTable;
    }

    public String getRightTable() {
        return rightTable;
    }

    public String getLeftColumn() {
        return leftColumn;
    }

    public String getRightColumn() {
        return rightColumn;
    }

    public JoinType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Join{" +
                "leftTable='" + leftTable + '\'' +
                ", rightTable='" + rightTable + '\'' +
                ", leftColumn='" + leftColumn + '\'' +
                ", rightColumn='" + rightColumn + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Join)) return false;
        Join join = (Join) o;
        return Objects.equal(getLeftTable(), join.getLeftTable()) && Objects.equal(getRightTable(), join.getRightTable()) && Objects.equal(getLeftColumn(), join.getLeftColumn()) && Objects.equal(getRightColumn(), join.getRightColumn()) && getType() == join.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getLeftTable(), getRightTable(), getLeftColumn(), getRightColumn(), getType());
    }
}
