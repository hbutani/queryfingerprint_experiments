package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import org.hatke.queryfingerprint.model.Queryfingerprint;

public class TestUtils {

    public static void showFingerPrints(QueryAnalysis qA) {
        QueryfingerprintBuilder qfpB = new QueryfingerprintBuilder(qA);
        ImmutableList<Queryfingerprint> fps = qfpB.build();
        for(Queryfingerprint qFP : fps) {
            System.out.println(qFP);
        }
    }
}
