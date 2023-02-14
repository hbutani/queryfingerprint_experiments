package benchmark;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import org.hatke.queryfingerprint.snowflake.parse.pool.QueryAnalysisPool;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
public class QueryAnalysisPooledState {

    public QueryAnalysisPool pool;
    private static TSQLEnv sqlEnv = new TPCDSSQLEnv(EDbVendor.dbvsnowflake);

    @Setup(Level.Invocation)
    public void doSetup() {
        pool = new QueryAnalysisPool(sqlEnv);
    }

    @TearDown(Level.Invocation)
    public void doTearDown() {
        pool.shutDown();
    }
}
