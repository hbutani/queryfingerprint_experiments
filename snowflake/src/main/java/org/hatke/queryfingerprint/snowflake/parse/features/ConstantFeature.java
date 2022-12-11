package org.hatke.queryfingerprint.snowflake.parse.features;

import gudusoft.gsqlparser.nodes.TExpression;

public class ConstantFeature extends BaseFeature {

    private final String value;

    ConstantFeature(TExpression expr, String value) {
        super(expr, ExprKind.constant);
        this.value = value;
    }

    public String getContantValue() {
        return value;
    }

    public String toString() {
        return String.format("constant:%1$s", value);
    }
}
