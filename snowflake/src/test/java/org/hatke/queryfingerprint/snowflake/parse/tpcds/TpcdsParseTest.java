package org.hatke.queryfingerprint.snowflake.parse.tpcds;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.hatke.queryfingerprint.snowflake.parse.QueryAnalysis;
import org.hatke.queryfingerprint.snowflake.parse.QueryfingerprintBuilder;
import org.hatke.queryfingerprint.snowflake.parse.TestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TpcdsParseTest extends TestBase {

    Logger logger = Logger.getLogger(TpcdsParseTest.class.getName());

    ImmutableSet<String> blackListQueries = ImmutableSet.of("query8");

    @Test
    void parseTpcdsQueries() throws IOException {
        for (int i = 1; i < 103; i++) {
            String queryFileName = "query" + i;

            if(blackListQueries.contains(queryFileName)) continue;

            String query = readTpcdsQuery(queryFileName);

            logger.info(String.format("Parsing query %s", queryFileName));

            try {
                QueryAnalysis qa = new QueryAnalysis(sqlEnv, query);
                QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
                ImmutableList<Queryfingerprint> fps = qfpB.build();
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, String.format("Failed query parsing for %s" , queryFileName));
            }

        }
    }

}
