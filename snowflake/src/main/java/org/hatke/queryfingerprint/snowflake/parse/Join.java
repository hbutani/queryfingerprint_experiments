package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;

public class Join {

    Source left;
    Source right;

    JoinType joinType;

    ImmutableList<Column> leftColumns;
    ImmutableList<Column> rightColumns;

}
