package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import org.hatke.queryfingerprint.model.QBType;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompositeQueryTest extends TestBase {

    @Test
    void testTpcdsQuery4() throws IOException {
        String query = readTpcdsQuery("query4");

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, query);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();
        assertEquals(fps.size(), 5);

        ImmutableList<Queryfingerprint> cteQueries = fps.stream().filter(f -> f.isCTE()).collect(ImmutableList.toImmutableList());
        assertEquals(cteQueries.size(), 4);

        ImmutableList<Queryfingerprint> compositeQuery = fps.stream().filter(f -> f.getType() == QBType.composite).collect(ImmutableList.toImmutableList());
        assertEquals(compositeQuery.size(), 1);

        // assert filter columns
        // assert join columns for both top and cte queries
        // assert scan columns

    }

    @Test
    void testTpcdsQuery8() throws IOException {
        String query = readTpcdsQuery("query58");

        QueryAnalysis qa = new QueryAnalysis(sqlEnv, query);
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
        ImmutableList<Queryfingerprint> fps = qfpB.build();
        assertEquals(fps.size(), 6);

//        ImmutableList<Queryfingerprint>  cteQueries = fps.stream().filter( f -> f.isCTE()).collect(ImmutableList.toImmutableList());
//        assertEquals(cteQueries.size(), 4);
//
//        ImmutableList<Queryfingerprint>  compositeQuery = fps.stream().filter( f -> f.getType() == QBType.composite).collect(ImmutableList.toImmutableList());
//        assertEquals(compositeQuery.size(), 1);

        // assert filter columns
        // assert join columns for both top and cte queries
        // assert scan columns

    }


}
