package org.hatke.queryfingerprint.snowflake.parse.pool;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.hatke.queryfingerprint.snowflake.parse.QueryAnalysis;
import org.hatke.queryfingerprint.snowflake.parse.QueryfingerprintBuilder;

import java.io.Serializable;

public class QueryAnalysisPool implements Serializable {
    private static final long serialVersionUID = 4177614865898145045L;
    public final TGSqlParserPool tgSqlParserPool;
    private final TSQLEnv env;

    public QueryAnalysisPool(TSQLEnv env) {
        this.env = env;
        tgSqlParserPool = new TGSqlParserPool(env.getDBVendor(), 1, 10000, 10);
    }

    public ImmutableList<Queryfingerprint> computeFingerPrint(String sql) {
        TGSqlParser tgSqlParser = tgSqlParserPool.borrowObject();
        QueryAnalysis qa = new QueryAnalysis(this.env, tgSqlParser, sql);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();
        tgSqlParserPool.returnObject(tgSqlParser);
        return fps;
    }

    public void shutDown() {
        tgSqlParserPool.shutdown();
    }
}

class TGSqlParserPool extends ObjectPool<TGSqlParser> implements Serializable {
    private static final long serialVersionUID = -9008322665791175647L;

    private final EDbVendor eDbVendor;

    public TGSqlParserPool(EDbVendor eDbVendor, int minObjects, int maxObjects, long validationIntervalInSeconds) {
        super(minObjects, maxObjects, validationIntervalInSeconds);
        this.eDbVendor = eDbVendor;
        initialize(minObjects);
    }

    @Override
    protected TGSqlParser createObject() {
        return new TGSqlParser(this.eDbVendor);
    }
}
