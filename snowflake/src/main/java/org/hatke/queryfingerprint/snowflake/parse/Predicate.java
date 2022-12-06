package org.hatke.queryfingerprint.snowflake.parse;

import java.util.Optional;

/**
 * represents a {@link Column} comparison against a value.
 * Optionally captures a function application on the Column.
 * Captures if the predicate is a topLevel conjunct or is part of a disjunct.
 */
public class Predicate {

    Column col;

    Optional<String> functionName;

    Optional<FunctionClass> functionClass;

    String value;

    boolean isTopLevelConjunct;
}
