package org.hatke.queryfingerprint.snowflake.parse.enums;

import gudusoft.gsqlparser.EComparisonType;

public enum EComparisonOperatorType {

    unknown,
    equals,
    nullSafeEquals,
    greaterThan,
    lessThan,
    greaterThanOrEqualTo,
    lessThanOrEqualTo,
    notEqualToBrackets,
    notEqualToExclamation,
    notEqualToCaret,
    notLessThan,
    notLessThanToCaret,
    notGreaterThan,
    notGreaterThanToCaret,
    leftOuterJoin,
    rightOuterJoin,
    includes,
    excludes,
    at,
    above,
    above_or_below,
    below,
    in,
    between;

    EComparisonOperatorType() {
    }

    public static EComparisonOperatorType from(EComparisonType type) {
        return EComparisonOperatorType.valueOf(type.toString());
    }
}

