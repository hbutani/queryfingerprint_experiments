package org.hatke.queryfingerprint.model;

import java.io.Serializable;
import java.util.Optional;

public class Predicate implements Serializable {

    private static final long serialVersionUID = 5007720260431090434L;

    private final Optional<String> functionName;
    private final String column;

    private final String operator;

    public Predicate(Optional<String> functionName, String column, String operator) {
        this.functionName = functionName;
        this.column = column;
        this.operator = operator;
    }

    public Optional<String> getFunctionName() {
        return functionName;
    }

    public String getColumn() {
        return column;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return "Predicate{" +
                Utils.optionalInfoString("functionName", functionName) +
                ", column='" + column + '\'' +
                ", operator='" + operator + '\'' +
                '}';
    }
}
