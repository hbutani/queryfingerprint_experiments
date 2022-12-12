package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.nodes.IExpressionVisitor;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TParseTreeNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class FindExpressionsByType implements IExpressionVisitor  {

    private final Map<EExpressionType, Set<TExpression>> exprMap = new HashMap<>();
    private final ImmutableList<EExpressionType> exprTypes;

    FindExpressionsByType(ImmutableList<EExpressionType> exprTypes) {
        this.exprTypes = exprTypes;
        for(EExpressionType eTyp : exprTypes) {
            exprMap.put(eTyp, new HashSet<>());
        }
    }


    @Override
    public boolean exprVisit(TParseTreeNode tParseTreeNode, boolean b) {
        TExpression expr = (TExpression) tParseTreeNode;
        if ( exprTypes.contains(expr.getExpressionType())) {
            exprMap.get(expr.getExpressionType()).add(expr);
            return false;
        }
        return true;
    }

    static ImmutableSet<TExpression> findSubqueries(TExpression expr) {
        FindExpressionsByType f = new FindExpressionsByType(ImmutableList.of(EExpressionType.subquery_t));
        expr.preOrderTraverse(f);

        return ImmutableSet.copyOf(f.exprMap.get(EExpressionType.subquery_t));
    }
}
