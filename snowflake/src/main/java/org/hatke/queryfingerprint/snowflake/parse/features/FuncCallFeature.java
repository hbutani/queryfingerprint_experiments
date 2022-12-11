package org.hatke.queryfingerprint.snowflake.parse.features;

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

    public Optional<ColumnRef> getColumnRef() {
        return colRef.getColumnRef();
    }

    public Optional<FuncCallFeature> getFuncCall() {
        return Optional.of(this);
    }

    public Optional<String> getFuncName() {
        return Optional.of(funcName);
    }

    public Optional<FunctionClass> getFuncClass() {
        return Optional.of(funcClass);
    }

    public String toString() {
        return String.format("function:name=%1$s, %2$s", funcName, colRef);
    }
}
