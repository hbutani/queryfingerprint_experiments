package org.hatke.queryfingerprint.snowflake.parse;

import java.util.Optional;

/**
 * Captures the application of a function on a {@link Column}
 */
public class FunctionApplication {

    Column col;

    String functionName;

    FunctionClass functionClass;
}
