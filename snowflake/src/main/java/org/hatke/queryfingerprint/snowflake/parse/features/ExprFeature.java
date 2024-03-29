package org.hatke.queryfingerprint.snowflake.parse.features;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.EComparisonType;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.nodes.TConstant;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TFunctionCall;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.sqlenv.ESQLDataObjectType;
import org.hatke.queryfingerprint.model.JoinType;
import org.hatke.queryfingerprint.snowflake.parse.Column;
import org.hatke.queryfingerprint.snowflake.parse.enums.EComparisonOperatorType;
import org.hatke.utils.Pair;
import org.hatke.queryfingerprint.snowflake.parse.ColumnRef;
import org.hatke.queryfingerprint.snowflake.parse.QB;
import org.hatke.queryfingerprint.snowflake.parse.Utils;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents different extracted features from a QueryBlock:
 * <br/>
 *  <li>A `column_ref` captures an input(or correlated) Column reference</li>
 *  <li>A `func_call` captures a application of a function on an input(or correlated) column</li>
 *  <li>A `predicate` captures a filter on an input(or correlated) column, optionally after
 *  column is applied on a function</li>
 *  <li>A `join` captures a join of 2 tables.</li>
 */
public interface ExprFeature {

    static final Logger LOGGER = Utils.getLogger();

    TExpression getTExpression();

    ExprKind getExprKind();

    default ImmutableList<ColumnRef> getColumnRefs() {
        return ImmutableList.of();
    }

    default boolean isSingleColumn() {
        ImmutableList<ColumnRef> cRs = getColumnRefs();
        return cRs.size() == 1;
    }

    default ImmutableList<FuncCallFeature> getFuncCalls() {
        return ImmutableList.of();
    }

    default boolean isSingleFuncCall() {
        ImmutableList<ColumnRef> cRs = getColumnRefs();
        return cRs.size() == 1;
    }

    default Optional<PredicateFeature> getPredicate() {
        return Optional.empty();
    }

    default Optional<FuncCallFeature> setFunctionCall(TExpression expr,
                                                      String funcName,
                                                      FunctionClass funcClass) {
        return Optional.empty();
    }

    static ExprFeature combineIntoPredOrJoin(QB qb,
                                             ExprFeature leftFeature,
                                             TExpression expr,
                                             EComparisonOperatorType compOp,
                                             ExprFeature rightFeature) {

        if (leftFeature == null || rightFeature == null) {
            return null;
        }

        if (leftFeature.isSingleColumn() && rightFeature.getExprKind() == ExprKind.constant) {
            return new PredicateFeature(expr, leftFeature, compOp, (ConstantFeature) rightFeature);
        } else if (rightFeature.isSingleColumn() && leftFeature.getExprKind() == ExprKind.constant) {
            return new PredicateFeature(expr, rightFeature, flipCompOp(compOp), (ConstantFeature) leftFeature);
        } else if (leftFeature.isSingleColumn() && rightFeature.isSingleColumn() &&
                compOp == EComparisonOperatorType.equals) {
            ColumnRef lColRef = leftFeature.getColumnRefs().get(0);
            ColumnRef rColRef = rightFeature.getColumnRefs().get(0);

            if (lColRef instanceof Column.CorrelateColRef && !(rColRef instanceof Column.CorrelateColRef) ) {
                return new CorrelateJoinFeature(expr, (Column.CorrelateColRef) lColRef, rColRef, qb);
            } else if (rColRef instanceof Column.CorrelateColRef && !(lColRef instanceof Column.CorrelateColRef) ) {
                return new CorrelateJoinFeature(expr, (Column.CorrelateColRef) rColRef, lColRef, qb);
            } else {

                Column lc = lColRef.getColumn();
                Column rc = rColRef.getColumn();
                if (lc.getSource().getId() != rc.getSource().getId()) {
                    return new JoinFeature(expr, leftFeature, rightFeature, JoinType.inner);
                }
            }
        }

        return null;
    }

    static FuncCallFeature buildFuncCall(TExpression expr,
                                         ExprFeature operandFeature,
                                         String funcName,
                                         FunctionClass funcClass) {
        if (operandFeature.getExprKind() == ExprKind.column_ref) {
            return new FuncCallFeature(expr, (ColRefFeature) operandFeature, funcName, funcClass);
        }
        return null;
    }

    static ExprFeature of(TExpression expr,
                           ColumnRef colRef) {
        return new ColRefFeature(expr, colRef);
    }

    static ExprFeature of(TExpression expr,
                           TConstant constant) {
        return new ConstantFeature(expr, constant.getValue());
    }

    static Function<Predicate<Void>, Boolean> check = (pred) -> pred.test(null);

    static EComparisonOperatorType flipCompOp(EComparisonOperatorType compOp) {
        switch (compOp) {
            case greaterThan:
                return EComparisonOperatorType.lessThan;
            case lessThan:
                return EComparisonOperatorType.greaterThan;
            case greaterThanOrEqualTo:
                return EComparisonOperatorType.lessThanOrEqualTo;
            case lessThanOrEqualTo:
                return EComparisonOperatorType.greaterThanOrEqualTo;
            default:
                return compOp;
        }
    }

    /**
     * - handles `col_ref = constant`, `constant = col_ref`
     * - doesn't handle `col_ref = ?`, ...
     *
     * @param expr
     * @return
     */
    static ExprFeature matchPredicate(TExpression expr, QB qb) {
        boolean validExpr = true;

        validExpr = validExpr &&
                check.apply(v -> expr.getExpressionType() == EExpressionType.simple_comparison_t);
        if (!validExpr) {
            return null;
        }

        TExpression leftOperand = expr.getLeftOperand();
        TExpression rightOperand = expr.getRightOperand();

        validExpr = validExpr &&
                check.apply(v -> leftOperand != null && rightOperand != null);
        if (!validExpr) {
            return null;
        }

        ExprFeature leftFeature = match(leftOperand, qb);
        ExprFeature rightFeature = match(rightOperand, qb);

        return combineIntoPredOrJoin(qb, leftFeature, expr,EComparisonOperatorType.from(expr.getComparisonType()), rightFeature);

    }

    static ExprFeature matchInPredicate(TExpression expr, QB qb) {
        boolean validExpr = true;

        validExpr = validExpr &&
                check.apply(v -> expr.getExpressionType() == EExpressionType.in_t);
        if (!validExpr) {
            return null;
        }

        TExpression leftOperand = expr.getLeftOperand();
        TExpression rightOperand = expr.getRightOperand();

        validExpr = validExpr &&
                check.apply(v -> leftOperand != null && rightOperand != null);
        if (!validExpr) {
            return null;
        }

        ExprFeature leftFeature = match(leftOperand, qb);

        return new PredicateFeature(expr, leftFeature, EComparisonOperatorType.in, new ConstantFeature(rightOperand, rightOperand.getCompactString()));

    }

//    static ExprFeature matchBetweenPredicate(TExpression expr, QB qb) {
//        boolean validExpr = true;
//
//        validExpr = validExpr &&
//                check.apply(v -> expr.getExpressionType() == EExpressionType.between_t);
//        if (!validExpr) {
//            return null;
//        }
//
//        TExpression betweenOperand = expr.getBetweenOperand();
//        TExpression rightOperand = expr.getRightOperand();
//        TExpression leftOperand = expr.getLeftOperand();
//
//        validExpr = validExpr &&
//                check.apply(v  -> betweenOperand != null && betweenOperand != null);
//        if (!validExpr) {
//            return null;
//        }
//
//        ExprFeature betweenFeature = match(betweenOperand, qb);
//
//        return new PredicateFeature(expr, betweenFeature, EComparisonOperatorType.between, new ConstantFeature(rightOperand, rightOperand.getCompactString()));
//    }


    static ExprFeature matchConstant(TExpression expr, QB qb) {

        if (expr.getExpressionType() == EExpressionType.simple_constant_t) {
            return of(expr, expr.getConstantOperand());
        } else {
            return null;
        }
    }

    static ExprFeature matchColumn(TExpression expr, QB qb) {
        if (expr.getExpressionType() == EExpressionType.simple_object_name_t) {
            TObjectName objectName = expr.getObjectOperand();
            Optional<ColumnRef> colRef = qb.resolveInputColumn(objectName);

            if (colRef.isPresent()) {
                return of(expr, colRef.get());
            } else {
//                LOGGER.info(
//                        String.format("Failed to resolve column %1$s in QB %2$d", objectName, qb.getId())
//                );
            }
        }
        return null;
    }

    static ExprFeature matchFunctionApply(TExpression expr, QB qb) {
        if (expr.getExpressionType() == EExpressionType.function_t) {
            TFunctionCall fCall = expr.getFunctionCall();
            TObjectName funcName = fCall.getFunctionName();
            Pair<String, String> normalizedFnNm =
                    Utils.normalizedName(qb.getSqlEnv(), funcName, ESQLDataObjectType.dotFunction);
            FunctionClass fCls = FunctionClass.fromFunctionName(normalizedFnNm.left);

            // Take first col feature. Nested and multi column feature not supported
            if (fCall.getArgs() != null) {
                for(int i=0; i < fCall.getArgs().size(); i++) {
                    ExprFeature operandFeature = match(fCall.getArgs().getExpression(0), qb);
                    if (operandFeature != null && operandFeature.getExprKind() == ExprKind.column_ref) {
                        return buildFuncCall(expr, operandFeature, normalizedFnNm.right, fCls);
                    }
                }
            }
        }

        return null;
    }

    static ExprFeature match(TExpression expr, QB qb) {
        ExprFeature eF = null;

        eF = matchPredicate(expr, qb);
        eF = eF == null ? matchFunctionApply(expr, qb) : eF;
        eF = eF == null ? matchColumn(expr, qb) : eF;
        eF = eF == null ? matchConstant(expr, qb) : eF;
        eF = eF == null ? matchInPredicate(expr, qb) : eF;
//        eF = eF == null ? matchBetweenPredicate(expr, qb) : eF;

        return eF;
    }

}