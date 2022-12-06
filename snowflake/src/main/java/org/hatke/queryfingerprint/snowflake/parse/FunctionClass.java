package org.hatke.queryfingerprint.snowflake.parse;

import gudusoft.gsqlparser.nodes.TFunctionCall;
import gudusoft.gsqlparser.nodes.TObjectName;

public enum FunctionClass {

    numeric, datetime, string, comparison, other, aggregate;


    static FunctionClass fromFunctionName(TFunctionCall fCall) {
        return other;
    }
}
