package org.hatke.queryfingerprint.model;

import com.google.common.collect.ImmutableSet;

import java.io.Serializable;

public class FunctionApplication implements Serializable {
    private static final long serialVersionUID = 8704387471047025031L;

    public static final ImmutableSet<String> AGG_FUNCTIONS = ImmutableSet.of("SUM", "COUNT", "MIN", "MAX", "AVG", "AVERAGE");

    private final String functionName;
    private final String column;

    public FunctionApplication(String functionName, String column) {
        this.functionName = functionName;
        this.column = column;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getColumn() {
        return column;
    }

    public boolean isAggregate() {
        return AGG_FUNCTIONS.contains(functionName);
    }

    @Override
    public String toString() {
        return "FunctionApplication{" +
                "functionName='" + functionName + '\'' +
                ", column='" + column + '\'' +
                '}';
    }
}
