package org.hatke.queryfingerprint.snowflake.parse.predicates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hatke.queryfingerprint.model.*;
import org.hatke.queryfingerprint.snowflake.parse.QueryAnalysis;
import org.hatke.queryfingerprint.snowflake.parse.QueryfingerprintBuilder;
import org.hatke.queryfingerprint.snowflake.parse.TestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class PredicateExpressionTest extends TestBase {


    @Test
    void simpleFilterExprTest() {
        String expr_q1 = "select * from date_dim where d_year =2001";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, expr_q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 2);

        Queryfingerprint qf = fps.get(0);

        // assert table name
        assertEquals(qf.getTablesReferenced(), ImmutableSet.of("TPCDS.DATE_DIM"));
        // asser filter and scanfilter column
        assertEquals(qf.getColumnsScanFiltered(), ImmutableSet.of("TPCDS.DATE_DIM.D_YEAR"));
        assertEquals(qf.getColumnsFiltered(), ImmutableSet.of("TPCDS.DATE_DIM.D_YEAR"));
    }


    @Test
    void filterExprTest() {
        String expr_q1 = "select * from date_dim" +
                " where (d_year =2001) and " +
                "(2 < d_month_seq or d_week_seq = 5)";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, expr_q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 2);

        Queryfingerprint qf = fps.get(0);

        // assert tablename
        assertEquals(qf.getTablesReferenced(), ImmutableSet.of("TPCDS.DATE_DIM"));
        // asser filter and scanfilter column
        assertEquals(qf.getColumnsScanFiltered(), ImmutableSet.of("TPCDS.DATE_DIM.D_YEAR"));
        assertEquals(qf.getColumnsFiltered(), ImmutableSet.of("TPCDS.DATE_DIM.D_YEAR", "TPCDS.DATE_DIM.D_WEEK_SEQ", "TPCDS.DATE_DIM.D_MONTH_SEQ"));
    }


    @Test
    void funcExprTest() {
        String expr_q1 = "select * from date_dim" + " where (year(d_date) != 2005) and (2 < d_month_seq) and (d_year =2001) and " + "(2 > d_month_seq or d_week_seq = 4)";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, expr_q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 2);

        Queryfingerprint qf = fps.get(0);

        ImmutableList<FunctionApplication> fas = qf.getFunctionApplications().asList();
        FunctionApplication fa = fas.get(0);

        // assert table name
        assertEquals(qf.getTablesReferenced(), ImmutableSet.of("TPCDS.DATE_DIM"));
        // asser filter and scanfilter column
        assertEquals(qf.getColumnsScanFiltered(), ImmutableSet.of("TPCDS.DATE_DIM.D_YEAR", "TPCDS.DATE_DIM.D_DATE", "TPCDS.DATE_DIM.D_MONTH_SEQ"));
        assertEquals(qf.getColumnsFiltered(), ImmutableSet.of("TPCDS.DATE_DIM.D_YEAR", "TPCDS.DATE_DIM.D_WEEK_SEQ", "TPCDS.DATE_DIM.D_DATE", "TPCDS.DATE_DIM.D_MONTH_SEQ"));
        // assert predicate function
        assertEquals(fa.getFunctionName(), "YEAR");
        assertEquals(fa.getColumn(), "TPCDS.DATE_DIM.D_DATE");
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

        assertEquals(fps.size(), 2);

        Queryfingerprint qf1 = fps.get(0);
        Queryfingerprint qf2 = fps.get(1);

        ImmutableList<FunctionApplication> fas1 = qf1.getFunctionApplications().asList();
        FunctionApplication fa1 = fas1.get(0);
        ImmutableList<FunctionApplication> fas2 = qf2.getFunctionApplications().asList();
        FunctionApplication fa2 = fas2.get(0);

        ImmutableList<Join> js1 = qf1.getJoins().asList();
        Join j1 = js1.get(0);

        // assert table name
        assertEquals(qf1.getTablesReferenced(), ImmutableSet.of("TPCDS.STORES_SALES"));
        assertEquals(qf2.getTablesReferenced(), ImmutableSet.of("TPCDS.STORES_SALES"));
        // assert predicate function
        assertEquals(fa1.getFunctionName(), "AVG");
        assertEquals(fa1.getColumn(), "TPCDS.STORES_SALES.SS_SALES_PRICE");
        assertEquals(fa2.getFunctionName(),"AVG");
        assertEquals(fa2.getColumn(), "TPCDS.STORES_SALES.SS_SALES_PRICE");
        // assert join
        assertEquals(j1.getLeftTable(), "TPCDS.STORES_SALES");
        assertEquals(j1.getRightTable(), "TPCDS.STORES_SALES");
        assertEquals(j1.getLeftColumn(), "TPCDS.STORES_SALES.SS_STORE_SK");
        assertEquals(j1.getRightColumn(), "TPCDS.STORES_SALES.SS_STORE_SK");
        assertEquals(j1.getType(), JoinType.inner);
        // assert subquery and correlated join
        assertEquals(qf2.getCorrelatedColumns(), ImmutableSet.of("TPCDS.STORES_SALES.SS_STORE_SK"));
        assertEquals(qf2.getType(), QBType.sub_query);
    }

    @Test
    void correlatedJoinInPredicateTest() {
        String q = "select a from R where exists (select b from T where b = R.a)";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 2);

        Queryfingerprint qf1 = fps.get(0);
        Queryfingerprint qf2 = fps.get(1);

        ImmutableList<Join> js1 = qf1.getJoins().asList();
        Join j1 = js1.get(0);

        // assert table name
        assertEquals(qf1.getTablesReferenced(), ImmutableSet.of("TPCDS.T","TPCDS.R"));
        assertEquals(qf2.getTablesReferenced(), ImmutableSet.of("TPCDS.T"));
        // assert join
        assertEquals(j1.getLeftTable(), "TPCDS.R");
        assertEquals(j1.getRightTable(), "TPCDS.T");
        assertEquals(j1.getLeftColumn(), "TPCDS.R.A");
        assertEquals(j1.getRightColumn(), "TPCDS.T.B");
        assertEquals(j1.getType(), JoinType.inner);
        // assert subquery and correlated join
        assertEquals(qf2.getCorrelatedColumns(), ImmutableSet.of("TPCDS.R.A"));
        assertEquals(qf2.getType(), QBType.sub_query);
    }
}

