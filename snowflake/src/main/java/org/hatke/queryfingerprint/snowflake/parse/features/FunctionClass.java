package org.hatke.queryfingerprint.snowflake.parse.features;

import com.google.common.collect.ImmutableSet;
import org.hatke.queryfingerprint.snowflake.parse.features.utils.FunctionClassUtil;

public enum FunctionClass {

    numeric, datetime, string, comparison, other, aggregate, collection;


    public static FunctionClass fromFunctionName(String fnName) {

        if (FunctionClassUtil.DateTimeFn.funcs.contains(fnName.toLowerCase())) {
            return datetime;
        } else if (FunctionClassUtil.StringFn.funcs.contains(fnName.toLowerCase())) {
            return string;
        } else if (FunctionClassUtil.NumericFn.funcs.contains(fnName.toLowerCase())) {
            return numeric;
        } else if (FunctionClassUtil.AggregateFn.funcs.contains(fnName.toLowerCase())) {
            return aggregate;
        } else if (FunctionClassUtil.CollectionFn.funcs.contains(fnName.toLowerCase())) {
            return collection;
        }

        return other;
    }
}
