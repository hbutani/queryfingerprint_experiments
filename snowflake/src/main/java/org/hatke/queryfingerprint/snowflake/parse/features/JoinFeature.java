package org.hatke.queryfingerprint.snowflake.parse.features;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.nodes.TExpression;
import org.hatke.queryfingerprint.model.JoinType;
import org.hatke.queryfingerprint.snowflake.parse.ColumnRef;

public class JoinFeature extends BaseFeature{
    private final ExprFeature leftFeature;
    private final ExprFeature rightFeature;

    private final JoinType joinType;

    public JoinFeature(TExpression expr,
                       ExprFeature leftFeature, ExprFeature rightFeature, JoinType joinType) {
        super(expr, ExprKind.join);
        this.leftFeature = leftFeature;
        this.rightFeature = rightFeature;
        this.joinType = joinType;
    }

    public ImmutableList<ColumnRef> getColumnRefs() {
        ImmutableList.Builder<ColumnRef> b = new ImmutableList.Builder<>();
        b.addAll(leftFeature.getColumnRefs());
        b.addAll(rightFeature.getColumnRefs());
        return b.build();
    }

    public ImmutableList<FuncCallFeature> getFuncCalls() {
        ImmutableList.Builder<FuncCallFeature> b = new ImmutableList.Builder<>();
        b.addAll(leftFeature.getFuncCalls());
        b.addAll(rightFeature.getFuncCalls());
        return b.build();
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
