package org.hatke.queryfingerprint.snowflake.parse.features;

import com.google.common.base.Objects;
import gudusoft.gsqlparser.nodes.TExpression;

abstract class BaseFeature implements ExprFeature {

    private final TExpression expr;

    private final ExprKind exprKind;

    BaseFeature(TExpression expr, ExprKind exprKind) {
        this.expr = expr;
        this.exprKind = exprKind;
    }

    public TExpression getTExpression() {
        return expr;
    }

    @Override
    public ExprKind getExprKind() {
        return exprKind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseFeature)) return false;
        BaseFeature that = (BaseFeature) o;
        return Objects.equal(getTExpression(), that.getTExpression()) && exprKind == that.exprKind;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTExpression(), exprKind);
    }
}
