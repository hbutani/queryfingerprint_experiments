package org.hatke.queryfingerprint.snowflake.parse.features;

import gudusoft.gsqlparser.EComparisonType;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.nodes.TConstant;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TFunctionCall;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.sqlenv.ESQLDataObjectType;
import org.hatke.utils.Pair;
import org.hatke.queryfingerprint.snowflake.parse.Column;
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
public class ExprFeature {
    private static final Logger LOGGER = Utils.getLogger();

    public final TExpression expr;

    public final ExprKind exprKind;
    public final Optional<ColumnRef> colRef;

    public final Optional<EComparisonType> comparisonType;
    public final Optional<String> constantValue;

    public final Optional<String> funcName;

    public final Optional<FunctionClass> funcClass;


    public final Optional<Column> rightColRef;

    public final Optional<JoinType> joinType;

    ExprFeature(TExpression expr,
                ExprKind exprKind,
                Optional<ColumnRef> colRef,
                Optional<String> constantValue,
                Optional<EComparisonType> comparisonType,
                Optional<String> funcName,
                Optional<FunctionClass> funcClass,
                Optional<Column> rightColRef,
                Optional<JoinType> joinType) {
        this.expr = expr;
        this.exprKind = exprKind;
        this.colRef = colRef;
        this.constantValue = constantValue;
        this.comparisonType = comparisonType;
        this.funcName = funcName;
        this.funcClass = funcClass;
        this.rightColRef = rightColRef;
        this.joinType = joinType;
    }

    ExprFeature(TExpression expr,
                Optional<ColumnRef> colRef) {
        this(expr, ExprKind.column_ref,
                colRef,
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(),
                Optional.empty());
        assert colRef.isPresent();
    }

    ExprFeature(TExpression expr,
                Optional<ColumnRef> colRef,
                String constant,
                EComparisonType comparisonType) {
        this(expr, ExprKind.predicate,
                colRef,
                Optional.of(constant), Optional.of(comparisonType),
                Optional.empty(), Optional.empty(),
                Optional.empty(),
                Optional.empty());
        assert colRef.isPresent();
    }

    ExprFeature(TExpression expr,
                TConstant constant) {
        this(expr, ExprKind.constant,
                Optional.empty(),
                Optional.of(constant.getValue()), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(),
                Optional.empty());
        assert colRef.isPresent();
    }

    ExprFeature setPredicate(TExpression expr,
                             EComparisonType compOp,
                             ExprFeature rightFeature) {
        if (rightFeature == null) {
            return null;
        }

        if (exprKind == ExprKind.column_ref || exprKind == ExprKind.func_call) {
            if (rightFeature.exprKind == ExprKind.constant) {
                return new ExprFeature(expr,
                        ExprKind.predicate,
                        this.colRef,
                        rightFeature.constantValue,
                        Optional.of(compOp),
                        this.funcName,
                        this.funcClass,
                        this.rightColRef,
                        this.joinType);
            }
        } else if (exprKind == ExprKind.constant) {
            if (rightFeature.exprKind == ExprKind.column_ref || rightFeature.exprKind == ExprKind.func_call) {
                return rightFeature.setPredicate(expr, flipCompOp(compOp), this);
            }
        }
        return null;
    }

    ExprFeature setFunctionCall(TExpression expr,
                                String funcName,
                                FunctionClass funcClass) {
        if (exprKind == ExprKind.column_ref) {
            return new ExprFeature(expr,
                    ExprKind.func_call,
                    this.colRef,
                    constantValue,
                    comparisonType,
                    Optional.of(funcName),
                    Optional.of(funcClass),
                    rightColRef,
                    joinType);
        }

        return null;
    }

    public boolean hasColumnRef() {
        return colRef.isPresent();
    }

    public boolean hasFunctionCall() {
        return funcName.isPresent();
    }

    public boolean isPredicate() {
        return exprKind == ExprKind.predicate;
    }

    public ExprFeature getFuncCall() {
        assert funcName.isPresent();

        if (exprKind == ExprKind.func_call) {
            return this;
        } else {
            return new ExprFeature(expr,
                    ExprKind.func_call,
                    this.colRef,
                    Optional.empty(),
                    Optional.empty(),
                    funcName,
                    funcClass,
                    Optional.empty(),
                    Optional.empty());
        }
    }

    @Override
    public String toString() {
        if (exprKind == ExprKind.column_ref) {
            return String.format("column:%1$s", colRef.get().getColumn().getFQN());
        } else if (exprKind == ExprKind.func_call) {
            return String.format("function:name=%1$s, column=%2$s", funcName.get(), colRef.get().getColumn().getFQN());
        } else if (exprKind == ExprKind.constant) {
            return String.format("constant:%1$s", constantValue.get());
        } else {
            if (funcName.isPresent()) {
                return String.format("predicate:function:name=%1$s,column=%2$s,op=%3$s,value=%4$s",
                        funcName.get(), colRef.get().getColumn().getFQN(),
                        comparisonType.get().name(), constantValue.get()
                );
            } else {
                return String.format("predicate:column=%1$s,op=%2$s, value=%3$s",
                        colRef.get().getColumn().getFQN(), comparisonType.get().name(), constantValue.get()
                );
            }
        }
    }

    private static Function<Predicate<Void>, Boolean> check = (pred) -> pred.test(null);

    public static EComparisonType flipCompOp(EComparisonType compOp) {
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
     * @param expr
     * @return
     */
    public static ExprFeature matchPredicate(TExpression expr, QB qb) {
        boolean validExpr = true;

        validExpr = validExpr &&
                check.apply(v -> expr.getExpressionType() == EExpressionType.simple_comparison_t);
        if (!validExpr) {return null;}

        TExpression leftOperand = expr.getLeftOperand();
        TExpression rightOperand = expr.getRightOperand();

        validExpr = validExpr &&
                check.apply(v -> leftOperand != null && rightOperand != null);
        if (!validExpr) {return null;}

        ExprFeature leftFeature = match(leftOperand, qb);
        ExprFeature rightFeature = match(rightOperand, qb);

        if (leftFeature != null) {
            return leftFeature.setPredicate(expr, expr.getComparisonType(), rightFeature);
        }

        return null;

    }

    public static ExprFeature matchConstant(TExpression expr, QB qb) {

        if (expr.getExpressionType() == EExpressionType.simple_constant_t) {
            return new ExprFeature(expr, expr.getConstantOperand());
        } else {
            return null;
        }
    }

    public static ExprFeature matchColumn(TExpression expr, QB qb) {
        if (expr.getExpressionType() == EExpressionType.simple_object_name_t) {
            TObjectName objectName = expr.getObjectOperand();
            Optional<ColumnRef> colRef = qb.resolveInputColumn(objectName);

            if (colRef.isPresent()) {
                return new ExprFeature(expr, colRef);
            } else {
                LOGGER.info(
                        String.format("Failed to resolve column %1$s in QB %2$d", objectName, qb.getId())
                );
            }
        }
        return null;
    }

    public static ExprFeature matchFunctionApply(TExpression expr, QB qb) {
        if (expr.getExpressionType() == EExpressionType.function_t) {
            TFunctionCall fCall = expr.getFunctionCall();
            TObjectName funcName = fCall.getFunctionName();
            Pair<String, String> normalizedFnNm =
                    Utils.normalizedName(qb.getSqlEnv(), funcName, ESQLDataObjectType.dotFunction);
            FunctionClass fCls = FunctionClass.fromFunctionName(normalizedFnNm.left);

            if (fCall.getArgs() != null && fCall.getArgs().size() == 1) {
                ExprFeature operandFeature = match(fCall.getArgs().getExpression(0), qb);

                if (operandFeature != null) {
                    return operandFeature.setFunctionCall(expr, normalizedFnNm.right, fCls);
                }
            }
        }

        return null;
    }

    public static ExprFeature match(TExpression expr, QB qb) {
        ExprFeature eF = null;

        eF = matchPredicate(expr, qb);
        eF = eF == null ? matchFunctionApply(expr, qb) : eF;
        eF = eF == null ? matchColumn(expr, qb) : eF;
        eF = eF == null ? matchConstant(expr, qb) : eF;

        return eF;
    }
}
