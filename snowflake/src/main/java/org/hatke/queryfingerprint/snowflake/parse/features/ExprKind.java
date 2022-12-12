package org.hatke.queryfingerprint.snowflake.parse.features;

public enum ExprKind {
    column_ref, func_call, predicate, join, correlate_join, constant, composite
}
