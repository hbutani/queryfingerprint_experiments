package org.hatke.queryfingerprint.snowflake.parse.features;

import gudusoft.gsqlparser.nodes.TExpression;

class JoinFeature extends BaseFeature{
    private final ExprFeature leftFeature;
    private final ExprFeature rightFeature;

    private final JoinType joinType;

    JoinFeature(TExpression expr, ExprKind exprKind,
                       ExprFeature leftFeature, ExprFeature rightFeature, JoinType joinType) {
        super(expr, exprKind);
        this.leftFeature = leftFeature;
        this.rightFeature = rightFeature;
        this.joinType = joinType;
    }

    public ExprFeature getLeftFeature() {
        return leftFeature;
    }

    public ExprFeature getRightFeature() {
        return rightFeature;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public String toString() {
        return String.format("join:%1$s,op=%2$s, %3$s", leftFeature, joinType.name(), rightFeature);
    }
}
