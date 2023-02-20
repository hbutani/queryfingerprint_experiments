package org.hatke.queryfingerprint.model;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.Optional;

public class Predicate implements Serializable {

    private static final long serialVersionUID = 5007720260431090434L;

    private final Optional<String> functionName;
    private final String column;

    private final String operator;

    private final Optional<String> constantValue;

    public Predicate(Optional<String> functionName, String column,
                     String operator, Optional<String> constantValue) {
        this.functionName = functionName;
        this.column = column;
        this.operator = operator;
        this.constantValue = constantValue;
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

    public Optional<String> getConstantValue() {
        return constantValue;
    }

    @Override
    public String toString() {
        return "Predicate{" +
                "functionName=" + functionName +
                ", column='" + column + '\'' +
                ", operator='" + operator + '\'' +
                ", constantValue=" + constantValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Predicate)) return false;
        Predicate predicate = (Predicate) o;
        return Objects.equal(getFunctionName(), predicate.getFunctionName()) &&
                Objects.equal(getColumn(), predicate.getColumn()) &&
                Objects.equal(getOperator(), predicate.getOperator()) &&
                Objects.equal(getConstantValue(), predicate.getConstantValue());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getFunctionName(), getColumn(), getOperator(), getConstantValue());
    }
}
