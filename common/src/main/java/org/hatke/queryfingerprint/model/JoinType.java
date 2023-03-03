package org.hatke.queryfingerprint.model;

public enum JoinType {

    inner(true),
    semi(false),
    leftouter(true),
    rightouter(true, leftouter),
    fullouter(true),
    left(true),
    right(true),
    cross(true),
    join(false);

    static {
        leftouter.flipType = rightouter;
    }

    private final boolean isFlippable;
    private JoinType flipType;

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
