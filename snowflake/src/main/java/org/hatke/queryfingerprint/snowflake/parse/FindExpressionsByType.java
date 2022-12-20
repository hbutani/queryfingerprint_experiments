package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
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

    static Set<TExpression> findSubqueries(TExpression expr) {
        FindExpressionsByType f = new FindExpressionsByType(
                ImmutableList.of(EExpressionType.subquery_t,
                        EExpressionType.exists_t)
        );
        expr.preOrderTraverse(f);

        Set<TExpression> r = new HashSet<>();
        for(Set<TExpression> s : f.exprMap.values()) {
            r.addAll(s);
        }


        return r;
    }
}
