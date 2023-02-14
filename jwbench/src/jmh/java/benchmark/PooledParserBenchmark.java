package benchmark;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.hatke.queryfingerprint.model.TpcdsUtils;
import org.hatke.queryfingerprint.snowflake.parse.QueryAnalysis;
import org.hatke.queryfingerprint.snowflake.parse.QueryfingerprintBuilder;
import org.hatke.queryfingerprint.snowflake.parse.pool.QueryAnalysisPool;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PooledParserBenchmark {

    private static TSQLEnv sqlEnv = new TPCDSSQLEnv(EDbVendor.dbvsnowflake);
    private static final int ThreadVariable = 2;


    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Threads(ThreadVariable)
    public void fingerprintSimpleQuery() throws IOException {
        // Query lrngth 700 chsrsd
        String query = TpcdsUtils.readTpcdsQuery("query7");//
        QueryAnalysis qa = new QueryAnalysis(sqlEnv, query);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Threads(ThreadVariable)
    public void fingerprintPooledSimpleQuery(QueryAnalysisPooledState pool) throws IOException {
        // Query lrngth 700 chsrsd
        String query = TpcdsUtils.readTpcdsQuery("query7");
        ImmutableList<Queryfingerprint> fps = pool.pool.computeFingerPrint(query);
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Threads(ThreadVariable)
    public void fingerprintQuery() throws IOException {
        String query = TpcdsUtils.readTpcdsQuery("query49");
        QueryAnalysis qa = new QueryAnalysis(sqlEnv, query);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Threads(ThreadVariable)
    public void fingerprintPooledQuery(QueryAnalysisPooledState pool) throws IOException {
        String query = TpcdsUtils.readTpcdsQuery("query49");
        ImmutableList<Queryfingerprint> fps = pool.pool.computeFingerPrint(query);
    }
}
