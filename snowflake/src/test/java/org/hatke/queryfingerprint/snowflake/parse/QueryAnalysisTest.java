package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import io.ad.parse.tpcds.utils.FingerPrintUtils;

public class QueryAnalysisTest {
    private static String basic_1 =
            "select d_date_sk, d.d_month_seq from date_dim d where d_year =2001 and d_month_seq = 2";

    private static String result_expressions =
            "select d_date_sk, d.d_month_seq, year(d_date_sk), month(d.d_date_sk) month_of," +
                    " min(d_date_sk) min_date, max(d.d_date_sk) max_date," +
                    " min(d.d_month_seq) + 1 min_date_offset" +
                    " from date_dim d ";


    private static String basic_join =
            "select d_date_sk, i_brand_id, xyz from date_dim, item where d_year =2001 and d_month_seq = 2";

    private static String basic_cte =
            "with abc as (select d_date_sk from date_dim d where d_year =2001 and d_month_seq = 2)\n" +
                    "select d_date_sk from abc";


    private static TSQLEnv sqlEnv = new TPCDSSQLEnv(EDbVendor.dbvsnowflake);

    private static void showExpressionTrees(TSelectSqlStatement stat) {
        TreeDump td = new TreeDump();

        if (stat.getResultColumnList() != null) {
            System.out.println("Result Expression trees:");
            stat.getResultColumnList().acceptChildren(td);
            System.out.println(td.getTreeString());
        }

        if (stat.getWhereClause() != null) {
            td = new TreeDump();
            System.out.println("Where Expression trees:");
            stat.getWhereClause().acceptChildren(td);
            System.out.println(td.getTreeString());
        }

        if (stat.getGroupByClause() != null) {
            td = new TreeDump();
            System.out.println("Group By trees:");
            stat.getGroupByClause().acceptChildren(td);
            System.out.println(td.getTreeString());
        }

    }

    public static void main(String[] args) {

        ImmutableList<String> queries = ImmutableList.of(
                TPCDSQueries.q1,
                // TPCDSQueries.q2,
                TPCDSQueries.q3,
                TPCDSQueries.q6,
                TPCDSQueries.q9
                // TPCDSQueries.q10
        );

        for(String q : queries) {
            QueryAnalysis qA = new QueryAnalysis(sqlEnv, q);
            Show.show(qA, System.out);
            System.out.println("-------------------------------------------------------------------------------");
            TestUtils.showFingerPrints(qA);
        }

//        showExpressionTrees(tpcdsQA.getTopLevelQB().getSelectStat());

        // exprAnalysis();

        // exprFuncApply();

        // subQueryInWhere();
    }

    private static void exprAnalysis() {
        String expr_q1 = "select * from date_dim" +
                " where (d_year =2001) and " +
                "(2 < d_month_seq or d_week_seq = ?)";

        QueryAnalysis tpcdsQA = new QueryAnalysis(sqlEnv, expr_q1);
        Show.show(tpcdsQA, System.out);
    }

    private static void exprFuncApply() {
        String expr_q1 = "select * from date_dim" +
                " where (year(d_date) != 2005) and (2 < d_month_seq) and (d_year =2001) and " +
                "(2 > d_month_seq or d_week_seq = ?)";

        QueryAnalysis tpcdsQA = new QueryAnalysis(sqlEnv, expr_q1);
        Show.show(tpcdsQA, System.out);
    }

    private static void subQueryInWhere() {
        String q1 =
                "select  ss_sales_price \n" +
                "from stores_sales ss\n" +
                "where ss.ss_sales_price > (select avg(ss_sales_price)*1.2\n" +
                "from stores_sales ss2\n" +
                "where ss.ss_store_sk = ss2.ss_store_sk)\n";

        QueryAnalysis tpcdsQA = new QueryAnalysis(sqlEnv, q1);
        Show.show(tpcdsQA, System.out);

        // FingerPrintUtils.printTree(q1);
    }
}
