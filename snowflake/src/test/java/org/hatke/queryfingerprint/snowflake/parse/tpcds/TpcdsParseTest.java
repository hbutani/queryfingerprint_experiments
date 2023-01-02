package org.hatke.queryfingerprint.snowflake.parse.tpcds;

import com.google.common.collect.ImmutableList;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.hatke.queryfingerprint.snowflake.parse.QueryAnalysis;
import org.hatke.queryfingerprint.snowflake.parse.QueryfingerprintBuilder;
import org.hatke.queryfingerprint.snowflake.parse.TestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Logger;

public class TpcdsParseTest extends TestBase {

    Logger logger = Logger.getLogger(TpcdsParseTest.class.getName());

    @Test
    void parseTpcdsQueries() throws IOException {
        for (int i = 1; i < 103; i++) {
            String queryFileName = "query" + i;

            String query = readTpcdsQuery(queryFileName);

            logger.info(String.format("Parsing query %s", queryFileName));

            QueryAnalysis qa = new QueryAnalysis(sqlEnv, query);
            QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qa);
            ImmutableList<Queryfingerprint> fps = qfpB.build();

        }
    }

}
