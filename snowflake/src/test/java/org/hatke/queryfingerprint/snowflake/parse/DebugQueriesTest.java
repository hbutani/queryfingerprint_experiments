package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hatke.queryfingerprint.model.Join;
import org.hatke.queryfingerprint.model.JoinType;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DebugQueriesTest extends TestBase {

    @Test
    void testMultiArgsFuncSubQuery4() throws IOException {
        String q = readDebugQuery("sub-query4");

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 1);

        Queryfingerprint qf0 = fps.get(0);
        assertEquals(qf0.getHash().toString(), "1504fdb9-ed44-3cde-9521-763663da5d5b");
        assertEquals(qf0.getTablesReferenced(), ImmutableSet.of("TPCDS.CUSTOMER_ADDRESS"));
        assertEquals(qf0.getColumnsScanned(), ImmutableSet.of("TPCDS.CUSTOMER_ADDRESS.CA_ZIP"));
        assertEquals(qf0.getColumnsFiltered(), ImmutableSet.of("TPCDS.CUSTOMER_ADDRESS.CA_ZIP"));
        assertEquals(qf0.getScanPredicates().size(), 1);
        assertEquals(qf0.getScanPredicates().stream().findFirst().get().getOperator(), "in");
    }

    @Test
    void testSubQuery8() throws IOException {
        String q = readDebugQuery("sub-query8");

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 3);

        Queryfingerprint qf0 = fps.get(0);

        System.out.println(fps.get(0));
        System.out.println(fps.get(1));

        assertEquals(qf0.getTablesReferenced(), ImmutableSet.of("TPCDS.CUSTOMER_ADDRESS", "TPCDS.CUSTOMER"));
        assertEquals(qf0.getColumnsScanned(), ImmutableSet.of("TPCDS.CUSTOMER_ADDRESS.CA_ZIP", "TPCDS.CUSTOMER.*", "TPCDS.CUSTOMER.C_PREFERRED_CUST_FLAG", "TPCDS.CUSTOMER.C_CURRENT_ADDR_SK", "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK"));
        assertEquals(qf0.getColumnsFiltered(), ImmutableSet.of("TPCDS.CUSTOMER.C_PREFERRED_CUST_FLAG"));
        assertEquals(qf0.getScanPredicates().size(), 1);
        assertEquals(qf0.getScanPredicates().stream().findFirst().get().getOperator(), "equals");
        assertEquals(qf0.getJoins().size(), 1);
        for(Join j : qf0.getJoins()) {
            j.equals(new Join("TPCDS.CUSTOMER", "TPCDS.CUSTOMER_ADDRESS",
                    "TPCDS.CUSTOMER.C_CURRENT_ADDR_SK", "TPCDS.CUSTOMER_ADDRESS.CA_ADDRESS_SK",
                    JoinType.inner));
        }
    }

    @Test
    void testSubQuery58() throws IOException {
        String q = readTpcdsQuery("query58");

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 3);
    }

}
