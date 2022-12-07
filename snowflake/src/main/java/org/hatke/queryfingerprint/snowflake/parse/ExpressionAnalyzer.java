package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.nodes.IExpressionVisitor;
import gudusoft.gsqlparser.nodes.TConstant;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TFunctionCall;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TParseTreeNode;
import org.hatke.queryfingerprint.snowflake.parse.features.ExprFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.ExprKind;
import org.hatke.utils.Pair;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ExpressionAnalyzer implements IExpressionVisitor {

    private static final Logger LOGGER = Utils.getLogger();

    private final QB qb;
    private final TExpression expr;

    private ImmutableList.Builder<ExprFeature> exprInfos = new ImmutableList.Builder();

//    private ExprFeature analyzeExpression(TExpression expr) {
//
//        if (expr.getExpressionType() == EExpressionType.simple_comparison_t) {
//            return matchPredicate(expr, qb);
//        } else if (expr.getExpressionType() == EExpressionType.function_t) {
//            TFunctionCall fCall = expr.getFunctionCall();
//            TObjectName funcName = fCall.getFunctionName();
//            FunctionClass fCls = FunctionClass.fromFunctionName(fCall);
//
//            if (fCls == FunctionClass.comparison) {
//                // one side is a Column or Function on a Column
//                // other side should be a constant.
//
//                // form a Predicate ExprInfo
//
//                // if both sides are Columns from different sources
//                // form a Join ExprInfo
//
//            } else if (fCall.getExprList() != null && fCall.getExprList().size() == 1) {
//                // only arg should be a Column
//
//                // form a FunctionApply ExprInfo
//            }
//
//        } else if (expr.getExpressionType() == EExpressionType.simple_object_name_t) {
//            Optional<Column> col = qb.resolveColumn(expr.getObjectOperand());
//            if (col.isPresent()) {
//                return new ExprFeature(expr, col.map(c -> (ColumnRef) c));
//            }
//        } else if (expr.getExpressionType() == EExpressionType.simple_constant_t) {
//            // form a Constant ExprInfo
//        }
//
//        return null;
//    }

    public ExpressionAnalyzer(QB qb, TExpression expr) {
        this.qb = qb;
        this.expr = expr;
    }

    public Pair<ExprKind, ImmutableList<ExprFeature>> analyze() {
        expr.preOrderTraverse(this);

        ImmutableList<ExprFeature> features = exprInfos.build();

        if (features.size() == 1 && features.get(0).expr == expr) {
            return Pair.pairOf(features.get(0).exprKind, features);
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
