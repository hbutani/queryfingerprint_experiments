package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hatke.queryfingerprint.model.FunctionApplication;
import org.hatke.queryfingerprint.model.Join;
import org.hatke.queryfingerprint.model.JoinType;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JoinTest extends TestBase {

    @Test
    void simpleJoinTest() {
        String q1 = "SELECT Orders.OrderID, Customers.CustomerName, Orders.OrderDate " +
                "FROM Orders " +
                "INNER JOIN Customers ON Orders.CustomerID=Customers.CustomerID";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 1);

        Queryfingerprint qf = fps.get(0);

        // assert table name
        assertEquals(qf.getTablesReferenced(), ImmutableSet.of("TPCDS.ORDERS", "TPCDS.CUSTOMERS"));
        assertEquals(qf.getJoins().size(), 1);
        Join join = qf.getJoins().stream().findFirst().get();
        assertEquals(join.getLeftTable(), "TPCDS.CUSTOMERS");
        assertEquals(join.getRightTable(), "TPCDS.ORDERS");
        assertEquals(join.getLeftColumn(), "TPCDS.CUSTOMERS.CUSTOMERID");
        assertEquals(join.getRightColumn(), "TPCDS.ORDERS.CUSTOMERID");
        assertEquals(join.getType(), JoinType.inner);

    }

    @Test
    void simpleJoinFuncTest() {
        String q1 = "SELECT Orders.OrderID, Customers.CustomerName, Orders.OrderDate " +
                "FROM Orders " +
                "LEFT JOIN Customers ON abs(Orders.CustomerID) = Customers.CustomerID";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 1);

        Queryfingerprint qf = fps.get(0);

        // assert table name
        assertEquals(qf.getTablesReferenced(), ImmutableSet.of("TPCDS.ORDERS", "TPCDS.CUSTOMERS"));
        assertEquals(qf.getJoins().size(), 1);
        Join join = qf.getJoins().stream().findFirst().get();
        FunctionApplication fa = qf.getFunctionApplications().stream().findFirst().get();
        assertEquals(fa.getFunctionName(), "ABS");
        assertEquals(fa.getColumn(), "TPCDS.ORDERS.CUSTOMERID");
        assertEquals(join.getLeftTable(), "TPCDS.CUSTOMERS");
        assertEquals(join.getLeftTable(), "TPCDS.CUSTOMERS");
        assertEquals(join.getRightTable(), "TPCDS.ORDERS");
        assertEquals(join.getLeftColumn(), "TPCDS.CUSTOMERS.CUSTOMERID");
        assertEquals(join.getRightColumn(), "TPCDS.ORDERS.CUSTOMERID");
        assertEquals(join.getType(), JoinType.left);

    }

    @Test
    void joinMultipleColumnsTest() {
        String q1 = "SELECT Orders.OrderID, Customers.CustomerName, Orders.OrderDate " +
                "FROM Orders " +
                "INNER JOIN Customers ON Orders.CustomerID = Customers.CustomerID AND Orders.CustomerName = Customers.Name ";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 1);
        Queryfingerprint qf = fps.get(0);
        assertEquals(qf.getJoins().size(), 2);

    }

    @Test
    void joinTest01() {
        String q1 = "select count(*) from PERFORMANCE_REGRESSION.PUBLIC.CUSTOMER_100GB_DATABRICKS_SEQ as customer " +
                " INNER JOIN PERFORMANCE_REGRESSION.PUBLIC.countrycsvext as country \n" +
                " on customer.C_BIRTH_COUNTRY = upper(country.NAME)";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 1);

        Queryfingerprint qf = fps.get(0);

        Join join = qf.getJoins().stream().findFirst().get();
        FunctionApplication fa = qf.getFunctionApplications().stream().findFirst().get();
        assertEquals(fa.getFunctionName(), "upper");
        assertEquals(fa.getColumn(), "TPCDS.country.NAME");
    }


    @Test
    void joinTest02() {
        String q1 = "select count(*) from TPCDS.TPCDS.customer as customer " +
                " INNER JOIN TPCDS.TPCDS.store_sales as country \n" +
                " on customer.c_first_name = upper(country.ss_item_sk)";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 1);

        Queryfingerprint qf = fps.get(0);

        Join join = qf.getJoins().stream().findFirst().get();
        FunctionApplication fa = qf.getFunctionApplications().stream().findFirst().get();
        assertEquals(fa.getFunctionName(), "upper");
        assertEquals(fa.getColumn(), "TPCDS.country.NAME");
    }

    @Test
    void joinWithConstantTest() {
        String q1 = "select * from A join B where A.x = 5";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        Queryfingerprint qf = fps.get(0);
        assertEquals(fps.size(), 1);
        assertEquals(fps.get(0).getJoins().size(), 0);
    }

    @Test
    void joinWithSameTableColumnTest() {
        String q1 = "select * from A join B on A.x = A.y";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 1);

        Queryfingerprint qf = fps.get(0);
        assertEquals(fps.size(), 1);
        assertEquals(qf.getJoins().size(), 0);
    }

    @Test
    void joinTwoTableTest() {
        String q1 = "select * from A join B on A.x = B.y  join C on  A.z = C.b";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 1);

        Queryfingerprint qf = fps.get(0);
        assertEquals(qf.getJoins().size(), 2);
    }
}
