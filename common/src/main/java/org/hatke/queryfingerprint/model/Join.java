package org.hatke.queryfingerprint.model;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;

public class Join implements Serializable, Comparable<Join>, Normalizable<Join>  {
    private static final long serialVersionUID = 1771503490575888160L;

    enum Type {
        inner(true),
        semi(false),
        leftOuter(true),
        rightOuter(true, leftOuter),
        fullOuter(true),
        cross(true);

        private final boolean isFlippable;
        private final Type flipType;

        Type(boolean isFlippable, Type flipType) {
            this.isFlippable = isFlippable;
            this.flipType = flipType;
        }

        Type(boolean isFlippable) {
            this.isFlippable = isFlippable;
            this.flipType = this;
        }

        public boolean isFlippable() {
            return isFlippable;
        }

        public Type flip() {
            return flipType;
        }
    }

    public static final class Condition implements Serializable, Comparable<Condition> {
        private static final long serialVersionUID = 1382946828217636364L;

        private final ColumnName left;
        private final ColumnName right;

        public Condition(ColumnName left, ColumnName right, Type type) {
            this.left = left;
            this.right = right;
        }

        public ColumnName getLeft() {
            return left;
        }

        public ColumnName getRight() {
            return right;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Condition condition = (Condition) o;
            return Objects.equal(getLeft(), condition.getLeft()) && Objects.equal(getRight(), condition.getRight());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getLeft(), getRight());
        }

        @Override
        public int compareTo(Condition o) {
            int c = this.left.compareTo(o.getLeft());

            if ( c == 0 ) {
                c = this.right.compareTo(o.getRight());
            }

            return c;
        }
    }

    private final TableSource left;
    private final TableSource right;
    private final ImmutableList<Condition> joinConditions;
    private final Type type;

    public Join(TableSource left, TableSource right, Type type, Condition... joinConditions) {
        this(left, right, type, ImmutableList.copyOf(joinConditions));
    }

    private Join(TableSource left, TableSource right, Type type, ImmutableList<Condition> joinConditions) {
        this.left = left;
        this.right = right;
        this.joinConditions = joinConditions;
        this.type = type;
    }

    public TableSource getLeft() {
        return left;
    }

    public TableSource getRight() {
        return right;
    }

    public ImmutableList<Condition> getJoinConditions() {
        return joinConditions;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Join join = (Join) o;
        return Objects.equal(getLeft(), join.getLeft()) && Objects.equal(right, join.right) && Objects.equal(getJoinConditions(), join.getJoinConditions()) && getType() == join.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getLeft(), right, getJoinConditions(), getType());
    }

    @Override
    public int compareTo(Join o) {
        int c = this.left.compareTo(o.getLeft());

        if ( c == 0 ) {
            c = this.right.compareTo(o.getRight());
        }

        if ( c == 0 ) {
            c = Utils.compareLists(this.joinConditions, o.getJoinConditions());
        }

        if ( c == 0 ) {
            c = this.type.compareTo(o.getType());
        }

        return c;
    }

    @Override
    public Join normalize() {
        int c = left.compareTo(right);
        TableSource nLeft = c > 0 ? right : left;
        TableSource nRight = c > 0 ? left : right;
        Type nType = c > 0 ? type.flip() : type;
        return new Join(nLeft, nRight, nType, Normalizable.normalize(joinConditions));
    }
}
