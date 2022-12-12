package org.hatke.queryfingerprint.snowflake.parse.features;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.nodes.TExpression;
import org.hatke.queryfingerprint.snowflake.parse.Column;
import org.hatke.queryfingerprint.snowflake.parse.ColumnRef;
import org.hatke.queryfingerprint.snowflake.parse.QB;

public class CorrelateJoinFeature extends BaseFeature {

    private final Column.CorrelateColRef colRef;

    private final ColumnRef childColRef;

    private final QB childQB;


    public CorrelateJoinFeature(TExpression expr,  Column.CorrelateColRef colRef, ColumnRef childColRef, QB childQB) {
        super(expr, ExprKind.correlate_join);
        this.colRef = colRef;
        this.childColRef = childColRef;
        this.childQB = childQB;
    }

    public String toString() {
        return String.format("correlate_join:col=%1$s,child_qb=%2$d, child_col=%3$s",
                colRef.getColumn().getFQN(), childQB.getId(), childColRef.getColumn().getFQN());
    }

    public ImmutableList<ColumnRef> getColumnRefs() {
        return ImmutableList.of(colRef);
    }

    public Column.CorrelateColRef getColRef() {
        return colRef;
    }

    public ColumnRef getChildColRef() {
        return childColRef;
    }

    public QB getChildQB() {
        return childQB;
    }
}
