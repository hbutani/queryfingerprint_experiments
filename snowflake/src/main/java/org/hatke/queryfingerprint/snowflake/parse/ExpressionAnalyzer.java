package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.nodes.IExpressionVisitor;
import gudusoft.gsqlparser.nodes.TCaseExpression;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TFunctionCall;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TParseTreeNode;
import org.hatke.utils.Pair;

import java.util.Optional;

public class ExpressionAnalyzer implements IExpressionVisitor {

    private final QB qb;
    private final TExpression expr;

    public enum ExprKind {
        column_ref, func_call, predicate, join, constant, composite
    }

    public static class ExprInfo {
        public TExpression expr;

        public ExprKind exprKind;
        public Optional<Column> colRef;

        public Optional<String>  funcName;

        public Optional<FunctionClass>  funcClass;

        public Optional<String> constantValue;

        public Optional<Column> rightColRef;

        ExprInfo(TExpression expr,
                        ExprKind exprKind,
                        Optional<Column> colRef,
                        Optional<String> funcName,
                        Optional<FunctionClass>  funcClass,
                        Optional<String> constantValue,
                        Optional<Column> rightColRef) {
            this.expr = expr;
            this.exprKind = exprKind;
            this.colRef = colRef;
            this.funcName = funcName;
            this.funcClass = funcClass;
            this.constantValue = constantValue;
            this.rightColRef = rightColRef;
        }

        ExprInfo(TExpression expr,
                 Optional<Column> colRef) {
            this(expr, ExprKind.column_ref, colRef, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            assert colRef.isPresent();
        }
    }

    private ImmutableList.Builder<ExprInfo> exprInfos = new ImmutableList.Builder();

    private ExprInfo analyzeExpression(TExpression expr) {

        if (expr.getExpressionType() == EExpressionType.function_t) {
            TFunctionCall fCall = expr.getFunctionCall();
            TObjectName funcName = fCall.getFunctionName();
            FunctionClass fCls = FunctionClass.fromFunctionName(fCall);

            if (fCls == FunctionClass.comparison) {
                // one side is a Column or Function on a Column
                // other side should be a constant.

                // form a Predicate ExprInfo

                // if both sides are Columns from different sources
                // form a Join ExprInfo

            } else if (fCall.getExprList() != null && fCall.getExprList().size() == 1) {
                // only arg should be a Column

                // form a FunctionApply ExprInfo
            }

        } else if (expr.getExpressionType() == EExpressionType.simple_object_name_t) {
            Optional<Column> col = qb.resolveColumn(expr.getObjectOperand());
            if (col.isPresent()) {
                return new ExprInfo(expr, col);
            }
        } else if (expr.getExpressionType() == EExpressionType.simple_constant_t) {
            // form a Constant ExprInfo
        }

        return null;
    }

    public ExpressionAnalyzer(QB qb, TExpression expr) {
        this.qb = qb;
        this.expr = expr;
    }

    public Pair<ExprKind, ImmutableList<ExprInfo>> analyze() {
        ExprInfo topEInfo = analyzeExpression(expr);
        if (topEInfo != null) {
            return Pair.pairOf(topEInfo.exprKind, ImmutableList.of(topEInfo));
        }

        expr.preOrderTraverse(this);

        return Pair.pairOf(ExprKind.composite, exprInfos.build());
    }

    @Override
    public boolean exprVisit(TParseTreeNode tParseTreeNode, boolean b) {

        ExprInfo eInfo = analyzeExpression((TExpression) tParseTreeNode);

        if (eInfo != null) {
            exprInfos.add(eInfo);
            return false;
        }

        return true;
    }
}
