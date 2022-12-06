package org.hatke.queryfingerprint.snowflake.parse;

import java.util.Optional;

public interface ColumnRef {

    Column getColumn();
    Optional<QB> appearsInQB();

    boolean isCorrelated();
}
