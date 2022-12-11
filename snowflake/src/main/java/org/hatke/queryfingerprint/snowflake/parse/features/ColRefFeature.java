package org.hatke.queryfingerprint.snowflake.parse.features;

import gudusoft.gsqlparser.nodes.TExpression;
import org.hatke.queryfingerprint.snowflake.parse.ColumnRef;

import java.util.Optional;

public class ColRefFeature  extends BaseFeature {

    private final ColumnRef colRef;

    ColRefFeature(TExpression expr, ColumnRef colRef) {
        super(expr, ExprKind.column_ref);
        this.colRef = colRef;
    }

    public Optional<ColumnRef> getColumnRef() {
        return Optional.of(colRef);
    }

    public String toString() {
        return String.format("column:%1$s", colRef.getColumn().getFQN());
    }
}
