package org.hatke.queryfingerprint.snowflake.parse.features;

import gudusoft.gsqlparser.EComparisonType;
import gudusoft.gsqlparser.nodes.TExpression;
import org.hatke.queryfingerprint.snowflake.parse.ColumnRef;

import java.util.Optional;
import java.util.function.Predicate;

public class PredicateFeature extends BaseFeature {

    private final ExprFeature colFeature;
    private final EComparisonType comparisonType;
    private final ConstantFeature valueFeature;

    public PredicateFeature(TExpression expr, ExprFeature colFeature,
                            EComparisonType comparisonType, ConstantFeature valueFeature) {
        super(expr, ExprKind.predicate);
        this.colFeature = colFeature;
        this.comparisonType = comparisonType;
        this.valueFeature = valueFeature;
    }

    public Optional<ColumnRef> getColumnRef() {
        return colFeature.getColumnRef();
    }

    public Optional<FuncCallFeature> getFuncCall() {
        return colFeature.getFuncCall();
    }

    public EComparisonType getComparisonType() {
        return comparisonType;
    }

    public ConstantFeature getValueFeature() {
        return valueFeature;
    }

    public Optional<PredicateFeature> getPredicate() {
        return Optional.of(this);
    }

    public String toString() {
        return String.format("predicate:%1$s,op=%2$s, %3$s", colFeature, comparisonType.name(), valueFeature);
    }
}
