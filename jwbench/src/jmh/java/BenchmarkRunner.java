import benchmark.TPCDSSQLEnv;
import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.hatke.queryfingerprint.model.TpcdsUtils;
import org.hatke.queryfingerprint.snowflake.parse.pool.QueryAnalysisPool;

public class BenchmarkRunner {
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
