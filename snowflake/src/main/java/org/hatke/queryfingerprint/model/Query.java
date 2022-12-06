package org.hatke.queryfingerprint.model;

import com.google.common.base.Objects;

public abstract class Query implements TableSource {

    public enum QueryType {
        top_level, sub_query, cte
    }

    private final String sqlText;
    private final QueryType qQueryType;

    private Query(String sqlText, QueryType qQueryType) {
        this.sqlText = sqlText;
        this.qQueryType = qQueryType;
    }

    public String getSqlText() {
        return sqlText;
    }

    public QueryType getqType() {
        return qQueryType;
    }

    public void setParentQuery(Query q) {
        throw new IllegalArgumentException(
                String.format("Cannot set parent query on a %1$s query", getqType())
        );
    }

    public Query getParentQuery() {
        throw new IllegalArgumentException(
                String.format("%1$s query has no parent query", getqType())
        );
    }

    public SourceType sourceType() {
        return SourceType.query;
    }

    public int compareSource(TableSource s) {
        if ( s instanceof Query) {
            Query q = (Query) s;
            int c = qQueryType.compareTo(q.getqType());
            if ( c == 0) {
                c = sqlText.compareTo(q.getSqlText());
            }
            return c;
        } else {
            return compareTo(s);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query query = (Query) o;
        return Objects.equal(getSqlText(), query.getSqlText()) && getqType() == query.getqType();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getSqlText(), getqType());
    }

    @Override
    public String toString() {
        return "Query{" +
                "sqlText='" + sqlText + '\'' +
                ", qType=" + qQueryType +
                '}';
    }

    public static Query topLevel(String sqlText) {
        return new TopLevelQuery(sqlText);
    }

    public static Query subquery(String sqlText) {
        return new SubQuery(sqlText);
    }

    public static Query cte(String sqlText) {
        return new CTE(sqlText);
    }

    static class TopLevelQuery extends Query {

        private TopLevelQuery(String sqlText) {
            super(sqlText, QueryType.top_level);
        }
    }

    abstract static class InnerQueryBlock extends Query {

        private Query parentQuery;

        private InnerQueryBlock(String sqlText, QueryType qQueryType) {
            super(sqlText, qQueryType);
        }

        @Override
        public Query getParentQuery() {
            return parentQuery;
        }

        @Override
        public void setParentQuery(Query parentQuery) {
            this.parentQuery = parentQuery;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            SubQuery subQuery = (SubQuery) o;
            return Objects.equal(getParentQuery(), subQuery.getParentQuery());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), getParentQuery());
        }
    }

    static class CTE extends InnerQueryBlock {
        private CTE(String sqlText) {
            super(sqlText, QueryType.cte);
        }
    }

    static class SubQuery extends InnerQueryBlock {
        private SubQuery(String sqlText) {
            super(sqlText, QueryType.cte);
        }
    }
}
