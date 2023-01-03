package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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

        assertEquals(fps.size(), 1);

        Queryfingerprint qf0 = fps.get(0);
        assertEquals(qf0.getTablesReferenced(), ImmutableSet.of("TPCDS.CUSTOMER_ADDRESS"));
        assertEquals(qf0.getColumnsScanned(), ImmutableSet.of("TPCDS.CUSTOMER_ADDRESS.CA_ZIP"));
        assertEquals(qf0.getColumnsFiltered(), ImmutableSet.of("TPCDS.CUSTOMER_ADDRESS.CA_ZIP"));
        assertEquals(qf0.getScanPredicates().size(), 1);
        assertEquals(qf0.getScanPredicates().stream().findFirst().get().getOperator(), "in");
    }

}
