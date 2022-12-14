package org.hatke.queryfingerprint.model;

import java.io.Serializable;

public class FunctionApplication implements Serializable {
    private static final long serialVersionUID = 8704387471047025031L;

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

    @Override
    public String toString() {
        return "FunctionApplication{" +
                "functionName='" + functionName + '\'' +
                ", column='" + column + '\'' +
                '}';
    }
}
