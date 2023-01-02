package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableMap;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import org.hatke.queryfingerprint.model.QBType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class QueryAnalysis {

    public static class Builder {
        /**
         * resolve names case sensitively or not.
         */
        private boolean nameCaseSensitive = false;

        private TGSqlParser tgSqlParser;
        private TSelectSqlStatement query;

        public boolean isNameCaseSensitive() {
            return nameCaseSensitive;
        }

        public void setNameCaseSensitive(boolean nameCaseSensitive) {
            this.nameCaseSensitive = nameCaseSensitive;
        }

        public TSelectSqlStatement getQuery() {
            return query;
        }

        public void setQuery(TGSqlParser tgSqlParser) {
            assert(tgSqlParser != null);
            assert(tgSqlParser.getErrorCount() == 0);
            assert(tgSqlParser.sqlstatements.size() == 1);
            assert (tgSqlParser.sqlstatements.get(0) instanceof TSelectSqlStatement);
            this.tgSqlParser = tgSqlParser;
            this.query = (TSelectSqlStatement) tgSqlParser.sqlstatements.get(0);
        }

        public void setQuery(TGSqlParser tgSqlParser, String sql) {
            // TODO
        }

        public QueryAnalysis build() {
            if (getQuery() == null) {
                throw new IllegalArgumentException("Cannot do Query Analysis without a parsed query.");
            }

            // TODO
            return null;
        }
    }


    private int idGen = 0;
    private final TSQLEnv sqlEnv;

    private final TSelectSqlStatement inputStat;

    private final QB topLevelQB;

    private Map<String, SourceRef> cteMap = new HashMap<>();

    int nextId() {
        return idGen++;
    }

    TSQLEnv getSqlEnv() {
        return sqlEnv;
    }

    void addCTE(SourceRef srcRef) {
        this.cteMap.put(srcRef.alias.get(), srcRef);
        this.cteMap.put(srcRef.fqAlias.get(), srcRef);
    }

    public Source getCTE(String fqn) {
        return cteMap.get(fqn);
    }

    ImmutableMap<String, SourceRef> ctes() {
        return ImmutableMap.copyOf(cteMap);
    }

    public QB getTopLevelQB() {
        return topLevelQB;
    }

    private TSelectSqlStatement setupTopLevelStat(String sql) {
        TGSqlParser sqlparser = new TGSqlParser(sqlEnv.getDBVendor());
        sqlparser.setSqlEnv(sqlEnv);

        sqlparser.sqltext = sql;
        int ret = sqlparser.parse();

        if (sqlparser.getErrorCount() != 0) {
            throw new IllegalArgumentException(
                    String.format("Parse error %1$s\n" +
                            "SQL:\n %2$s\n",
                            sqlparser.getErrormessage(),
                            sql)
            );
        }

        if (sqlparser.sqlstatements.size() > 1) {
            throw new IllegalArgumentException(
                    String.format("Current support for single statements only\n" +
                                    "SQL:\n %1$s\n",
                            sql)
            );
        }

        if (!(sqlparser.sqlstatements.get(0) instanceof TSelectSqlStatement)) {
            throw new IllegalArgumentException(
                    String.format("Current support for select statements only\n" +
                                    "SQL:\n %1$s\n",
                            sql)
            );
        }


        return (TSelectSqlStatement) sqlparser.sqlstatements.get(0);
    }

    public QueryAnalysis(TSQLEnv sqlEnv, String sql) {
        this.sqlEnv = sqlEnv;
        this.inputStat = setupTopLevelStat(sql);
        this.topLevelQB = QB.create(this, true, QBType.regular, inputStat,
                Optional.empty(), Optional.empty());
    }

/*
    private QB constructQB(TSelectSqlStatement pTree,
                           Optional<String> alias,
                           Optional<QB> parentQB,
                           QBType type) {
        QB qb = null;
        qb.id = queryBlocks.size();
        queryBlocks.add(qb);
        return null;
    }

    private int addCTE(TCTE cte) {
        QB qb = constructQB(cte.getSubquery(),
                Optional.of(cte.getTableName().toString()), // TODO removeQuote, see initCTE in ColumnImpact
                null,
                QBType.cte);
        return qb.id;
    }

    private int addSubqueryBlock(TSelectSqlStatement pTree,
                                 Optional<String> alias,
                                 Optional<QB> parentQB) {
        return -1;
    }
*/
}
