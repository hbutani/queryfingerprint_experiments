package benchmark;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.dlineage.DataFlowAnalyzer;
import gudusoft.gsqlparser.dlineage.dataflow.model.xml.dataflow;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import gudusoft.gsqlparser.stmt.*;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.hatke.queryfingerprint.model.TpcdsUtils;
import org.hatke.queryfingerprint.snowflake.parse.QueryAnalysis;
import org.hatke.queryfingerprint.snowflake.parse.QueryfingerprintBuilder;
import org.hatke.queryfingerprint.snowflake.parse.TextBasedFingerprint;
import org.openjdk.jmh.annotations.*;
import utils.TableAnalyser;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

public class ParserBenchmark {

    private static TSQLEnv sqlEnv = new TPCDSSQLEnv(EDbVendor.dbvsnowflake);
    private static final int ThreadVariable = 1;


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
    public void parseSimpleQuery() throws IOException {
        String sql = TpcdsUtils.readTpcdsQuery("query7");
        //String sql="select * from stud";
        TGSqlParser sqlparser = new TGSqlParser(sqlEnv.getDBVendor());
        sqlparser.setSqlEnv(sqlEnv);

        sqlparser.sqltext = sql;
        int ret = sqlparser.parse();

    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Threads(ThreadVariable)
    public void textFPSimpleQuery() throws IOException {
        String sql = TpcdsUtils.readTpcdsQuery("query7");
        try {
            String hash = new TextBasedFingerprint().generate(EDbVendor.dbvsnowflake, sql);
        } catch (Exception e) {
            //
        }
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Threads(ThreadVariable)
    public void dataflowSimpleQuery() throws IOException {
        String sql = TpcdsUtils.readTpcdsQuery("query7");//14
        //String sql="select * from stud";
        DataFlowAnalyzer dataFlowAnalyzer = new DataFlowAnalyzer(sql, sqlEnv.getDBVendor(), true);
        dataFlowAnalyzer.setShowCountTableColumn(true);
        dataFlowAnalyzer.setShowJoin(false);
        dataFlowAnalyzer.setLinkOrphanColumnToFirstTable(false);
        dataFlowAnalyzer.setIgnoreRecordSet(true);
        dataFlowAnalyzer.setIgnoreCoordinate(true);
        dataFlowAnalyzer.generateDataFlow();
        dataflow dataFlow = dataFlowAnalyzer.getDataFlow();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Threads(ThreadVariable)
    public void analyseTablesSimpleQuery() throws IOException {
        String sql = TpcdsUtils.readTpcdsQuery("query7");
        TGSqlParser sqlparser = new TGSqlParser(sqlEnv.getDBVendor());
        sqlparser.setSqlEnv(sqlEnv);

        sqlparser.sqltext = sql;
        int ret = sqlparser.parse();
        analyseTables(sqlparser);
    }


    public static HashSet<String> analyseTables(TGSqlParser tgSqlParser){
        TableAnalyser tableAnalyser = new TableAnalyser(tgSqlParser);
        LinkedHashSet<String> tables = new LinkedHashSet<>();
        tables.addAll(tableAnalyser.analyse());
        for(TCustomSqlStatement sql : tgSqlParser.sqlstatements){
            sql.tables.forEach(it->tables.add(it.getFullName()));
            if(sql instanceof TCreateViewSqlStatement) {
                tables.add(((TCreateViewSqlStatement) sql).getViewName().toString());
                tableAnalyser.analyzeStatement(((TCreateViewSqlStatement) sql).getSubquery(),tables);
                //tables.addAll(analyzeStatement(stmt.subquery))
            }
            if(sql instanceof TAlterViewStatement) {
                //tables.add(stmt.viewName.toString())
                //tables.addAll(analyzeStatement(stmt.selectSqlStatement))
                tables.add(((TAlterViewStatement) sql).getViewName().toString());
                tableAnalyser.analyzeStatement(((TAlterViewStatement) sql).getSelectSqlStatement(),tables);
            }
            if(sql instanceof TCreateMaterializedSqlStatement) {
                //tables.add(stmt.viewName.toString())
                //tables.addAll(analyzeStatement(stmt.subquery))
                tables.add(((TCreateMaterializedSqlStatement) sql).getViewName().toString());
                tableAnalyser.analyzeStatement(((TCreateMaterializedSqlStatement) sql).getSubquery(),tables);
            }
            if(sql instanceof TAlterMaterializedViewStmt) {
                //setOf(stmt.materializedViewName.toString())
                //tables.addAll(analyzeStatement(stmt.topStatement))
                tables.add(((TAlterMaterializedViewStmt) sql).getMaterializedViewName().toString());
                tableAnalyser.analyzeStatement(sql.getTopStatement(),tables);
            }
            if(sql instanceof TCreateTableSqlStatement) {
                tables.add(((TCreateTableSqlStatement) sql).getTables().toString());
                if(((TCreateTableSqlStatement) sql).getLikeTableName() != null){
                    //CREATE TABLE LIKE
                    tables.add(((TCreateTableSqlStatement) sql).getTableName().toString());
                    tables.add(((TCreateTableSqlStatement) sql).getLikeTableName().toString());
                }else if(((TCreateTableSqlStatement) sql).getAsTable() != null) {
                    //CREATE TABLE AS
                    tables.add(((TCreateTableSqlStatement) sql).getTableName().toString());
                    tables.add(((TCreateTableSqlStatement) sql).getAsTable().toString());
                }
                if(((TCreateTableSqlStatement) sql).getSubQuery() != null) {
                    tableAnalyser.analyzeStatement(((TCreateTableSqlStatement) sql).getSubQuery(),tables);
                }
            }
//            if(sql instanceof TCopyIntoStmt) {
//                if(((TCopyIntoStmt) sql).getCopyIntoType() ==
//                        TCopyIntoStmt.COPY_INTO_TABLE){
//                    tables.add(((TCopyIntoStmt) sql).getTableName().toString());
//                }
//                //tables.add(((TCopyIntoStmt) sql).getTableName());
//            }
            if(sql instanceof TInsertSqlStatement) {
                tables.add(sql.getTables().toString());
                tableAnalyser.analyzeStatement(((TInsertSqlStatement) sql).getSubQuery(),tables);
            }
        }
        return tables;
    }
}





