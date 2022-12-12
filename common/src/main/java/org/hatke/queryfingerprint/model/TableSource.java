package org.hatke.queryfingerprint.model;

public interface TableSource extends Comparable<TableSource> {

    enum SourceType {
        table, view, query
    }

    SourceType sourceType();

    int compareSource(TableSource s);

    @Override
    default int compareTo(TableSource o) {
        int c = sourceType().compareTo(o.sourceType());
        if (c == 0) {
            c = compareSource(o);
        }
        return c;
    }
}
