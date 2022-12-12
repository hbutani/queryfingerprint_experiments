package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.nodes.IExpressionVisitor;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TParseTreeNode;
import org.hatke.queryfingerprint.snowflake.parse.features.ExprFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.ExprKind;
import org.hatke.utils.Pair;
import org.slf4j.Logger;

public class ExpressionAnalyzer implements IExpressionVisitor {

    private static final Logger LOGGER = Utils.getLogger();

    private final QB qb;
    private final TExpression expr;

    private ImmutableList.Builder<ExprFeature> exprInfos = new ImmutableList.Builder();


    public ExpressionAnalyzer(QB qb, TExpression expr) {
        this.qb = qb;
        this.expr = expr;
    }

    public Pair<ExprKind, ImmutableList<ExprFeature>> analyze() {
        expr.preOrderTraverse(this);

        ImmutableList<ExprFeature> features = exprInfos.build();

        if (features.size() == 1 && features.get(0).getTExpression() == expr) {
            return Pair.pairOf(features.get(0).getExprKind(), features);
        } else {
            return Pair.pairOf(ExprKind.composite, features);
        }

    }

    @Override
    public boolean exprVisit(TParseTreeNode tParseTreeNode, boolean b) {

        TExpression expr = (TExpression) tParseTreeNode;
        ExprFeature eInfo = ExprFeature.match(expr, qb);

        if (eInfo != null) {
            exprInfos.add(eInfo);
            expr.setVisitSubTree(false);
        }

        return true;
    }
}
