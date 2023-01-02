package org.hatke.queryfingerprint.snowflake.parse.predicates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.hatke.queryfingerprint.snowflake.parse.QueryAnalysis;
import org.hatke.queryfingerprint.snowflake.parse.QueryfingerprintBuilder;
import org.hatke.queryfingerprint.snowflake.parse.TestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PredicateExpressionTest extends TestBase {


    @Test
    void simpleFilterExprTest() {
        String expr_q1 = "select * from date_dim where d_year =2001";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, expr_q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertTrue(fps.size() == 1);

        Queryfingerprint qf = fps.get(0);

        assertEquals(qf.getColumnsScanFiltered(), ImmutableSet.of("TPCDS.DATE_DIM.D_YEAR"));
        assertEquals(qf.getColumnsFiltered(), ImmutableSet.of("TPCDS.DATE_DIM.D_YEAR"));

        // assert table name
    }


    @Test
    void filterExprTest() {
        String expr_q1 = "select * from date_dim" +
                " where (d_year =2001) and " +
                "(2 < d_month_seq or d_week_seq = 5)";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, expr_q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        Queryfingerprint qf = fps.get(0);

        assertEquals(qf.getColumnsScanFiltered(), ImmutableSet.of("TPCDS.DATE_DIM.D_YEAR"));
        assertEquals(qf.getColumnsFiltered(), ImmutableSet.of("TPCDS.DATE_DIM.D_YEAR", "TPCDS.DATE_DIM.D_WEEK_SEQ", "TPCDS.DATE_DIM.D_MONTH_SEQ"));

        // assert tablename
    }


    @Test
    void funcExprTest() {
        String expr_q1 = "select * from date_dim" + " where (year(d_date) != 2005) and (2 < d_month_seq) and (d_year =2001) and " + "(2 > d_month_seq or d_week_seq = 4)";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, expr_q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        // assert table name
        // asser filter and scanfilter column
        // assert predicate function
    }

    @Test
    void subQueryInPredicateTest() {
        String q1 =
                "select  ss_sales_price \n" +
                        "from stores_sales ss\n" +
                        "where ss.ss_sales_price > (select avg(ss_sales_price)*1.2\n" +
                        "from stores_sales ss2\n" +
                        "where ss.ss_store_sk = ss2.ss_store_sk)\n";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        // asser filter and scanfilter column
        // assert sub query and function in preojection
    }

    @Test
    void correlatedJoinInPredicateTest() {
        String q = "select a from R where exists (select b from T where b = R.a)";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        // asser filter and scanfilter column
        // assert subquery and correlated join
    }



}

