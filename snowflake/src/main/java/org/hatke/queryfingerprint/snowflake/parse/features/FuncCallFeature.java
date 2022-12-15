package org.hatke.queryfingerprint.snowflake.parse.features;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.nodes.TExpression;
import org.hatke.queryfingerprint.snowflake.parse.ColumnRef;

import java.util.Optional;

public class FuncCallFeature extends BaseFeature {

    private final ColRefFeature colRef;

    private final String funcName;

    private final FunctionClass funcClass;

    FuncCallFeature(TExpression expr, ColRefFeature colRef,
                           String funcName, FunctionClass funcClass) {
        super(expr, ExprKind.func_call);
        this.colRef = colRef;
        this.funcName = funcName;
        this.funcClass = funcClass;
    }

    public ImmutableList<ColumnRef> getColumnRefs() {
        return colRef.getColumnRefs();
    }

    public ImmutableList<FuncCallFeature> getFuncCalls() {
        return ImmutableList.of(this);
    }

    public String getFuncName() {
        return funcName;
    }

    public FunctionClass getFuncClass() {
        return funcClass;
    }

    public String toString() {
        return String.format("function:name=%1$s, %2$s", funcName, colRef);
    }
}
