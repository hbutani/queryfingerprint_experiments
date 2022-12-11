package org.hatke.queryfingerprint.snowflake.parse.features;

import gudusoft.gsqlparser.EComparisonType;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.nodes.TConstant;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TFunctionCall;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.sqlenv.ESQLDataObjectType;
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

    default Optional<ColumnRef> getColumnRef() {
        return Optional.empty();
    }


    default Optional<FuncCallFeature> getFuncCall() {
        return Optional.empty();
    }

    default Optional<FuncCallFeature> setFunctionCall(TExpression expr,
                                                      String funcName,
                                                      FunctionClass funcClass) {
        return Optional.empty();
    }

    static PredicateFeature combineIntoPred(ExprFeature leftFeature,
                                            TExpression expr,
                                            EComparisonType compOp,
                                            ExprFeature rightFeature) {

        if (leftFeature == null || rightFeature == null) {
            return null;
        }

        if (leftFeature.getColumnRef().isPresent() && rightFeature.getExprKind() == ExprKind.constant) {
            return new PredicateFeature(expr, leftFeature, compOp, (ConstantFeature) rightFeature);
        } else if (rightFeature.getColumnRef().isPresent() &&
                leftFeature.getExprKind() == ExprKind.constant) {
            return new PredicateFeature(expr, rightFeature, flipCompOp(compOp), (ConstantFeature) leftFeature);
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

    static EComparisonType flipCompOp(EComparisonType compOp) {
        switch (compOp) {
            case greaterThan:
                return EComparisonType.lessThan;
            case lessThan:
                return EComparisonType.greaterThan;
            case greaterThanOrEqualTo:
                return EComparisonType.lessThanOrEqualTo;
            case lessThanOrEqualTo:
                return EComparisonType.greaterThanOrEqualTo;
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

        return combineIntoPred(leftFeature, expr, expr.getComparisonType(), rightFeature);

    }

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
                LOGGER.info(
                        String.format("Failed to resolve column %1$s in QB %2$d", objectName, qb.getId())
                );
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

            if (fCall.getArgs() != null && fCall.getArgs().size() == 1) {
                ExprFeature operandFeature = match(fCall.getArgs().getExpression(0), qb);

                if (operandFeature != null) {
                    return buildFuncCall(expr, operandFeature, normalizedFnNm.right, fCls);
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

        return eF;
    }

}