package org.hatke.queryfingerprint.snowflake.parse;

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
}
