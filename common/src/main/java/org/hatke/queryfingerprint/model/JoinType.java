package org.hatke.queryfingerprint.model;

public enum JoinType {

    inner(true),
    semi(false),
    leftOuter(true),
    rightOuter(true, leftOuter),
    fullOuter(true),
    cross(true);

    private final boolean isFlippable;
    private final JoinType flipType;

    JoinType(boolean isFlippable, JoinType flipType) {
        this.isFlippable = isFlippable;
        this.flipType = flipType;
    }

    JoinType(boolean isFlippable) {
        this.isFlippable = isFlippable;
        this.flipType = this;
    }

    public boolean isFlippable() {
        return isFlippable;
    }

    public JoinType flip() {
        return flipType;
    }

}
