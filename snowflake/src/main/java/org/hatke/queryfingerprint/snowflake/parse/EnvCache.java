package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.hatke.utils.Pair;

import java.util.List;

public class EnvCache {

    public static Cache<String, Pair<String, String>> normalizedTableName =
            CacheBuilder.newBuilder()
                    .maximumSize(100000)
                    .build();

    public static Cache<String, List<String>> normalizedColName =
            CacheBuilder.newBuilder()
                    .maximumSize(100000)
                    .build();


}
