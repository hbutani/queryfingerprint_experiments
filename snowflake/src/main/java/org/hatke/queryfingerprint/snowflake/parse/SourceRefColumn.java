package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.base.Objects;
import gudusoft.gsqlparser.util.SQLUtil;

import java.util.List;
import java.util.stream.Collectors;

public class SourceRefColumn implements Column {

    final SourceRef srcRef;
    final Column sourceColumn;
    final String fqName;

    SourceRefColumn(SourceRef srcRef,
                    Column sourceColumn,
                    String srcAlias) {
        this.srcRef = srcRef;
        this.sourceColumn = sourceColumn;

        List<String> segments = SQLUtil.parseNames(sourceColumn.getFQN());

        if (segments.size() > 1) {
            segments.set(segments.size() - 2, srcAlias);
            fqName = segments.stream().collect(Collectors.joining("."));
        } else {
            fqName = sourceColumn.getFQN();
        }
    }

    @Override
    public int getId() {
        return sourceColumn.getId();
    }

    @Override
    public String getName() {
        return sourceColumn.getName();
    }

    @Override
    public String getFQN() {
        return fqName;
    }

    @Override
    public Source getSource() {
        return srcRef;
    }

    public Column getSourceColumn() {
        return sourceColumn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceRefColumn that = (SourceRefColumn) o;
        return Objects.equal(srcRef, that.srcRef) && Objects.equal(getSourceColumn(), that.getSourceColumn());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(srcRef, getSourceColumn());
    }
}
