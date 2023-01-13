package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GroupByTest extends TestBase {

    @Test
    void simpleGroupByTest() {
        String q1 = "select * from date_dim where d_year =2001 group by d_date, d_month_seq order by d_date desc";

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 1);

        Queryfingerprint qf = fps.get(0);

        // assert table name
        assertEquals(qf.getTablesReferenced(), ImmutableSet.of("TPCDS.DATE_DIM"));
        assertEquals(qf.getGroupedColumns(), ImmutableSet.of("TPCDS.DATE_DIM.D_DATE", "TPCDS.DATE_DIM.D_MONTH_SEQ" ) );
        assertEquals(qf.getOrderedColumns(), ImmutableSet.of("TPCDS.DATE_DIM.D_DATE" ) );

    }


    @Test
    void simpleTpcdsQuery17() throws IOException {
        String q1 = readTpcdsQuery("query17");

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, q1);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();

        assertEquals(fps.size(), 1);

        Queryfingerprint qf = fps.get(0);

        // assert table name
        assertEquals(qf.getTablesReferenced(), ImmutableSet.of("TPCDS.STORE_SALES", "TPCDS.ITEM", "TPCDS.CATALOG_SALES",
                "TPCDS.STORE_RETURNS", "TPCDS.DATE_DIM", "TPCDS.STORE"));
        assertEquals(qf.getGroupedColumns(), ImmutableSet.of("TPCDS.STORE.S_STATE", "TPCDS.ITEM.I_ITEM_ID", "TPCDS.ITEM.I_ITEM_DESC") );
        assertEquals(qf.getOrderedColumns(), ImmutableSet.of("TPCDS.STORE.S_STATE", "TPCDS.ITEM.I_ITEM_ID", "TPCDS.ITEM.I_ITEM_DESC") );

    }
}
