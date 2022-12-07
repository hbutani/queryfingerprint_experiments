package org.hatke.queryfingerprint.snowflake.parse.features;

import com.google.common.collect.ImmutableSet;

public enum FunctionClass {

    numeric, datetime, string, comparison, other, aggregate;


    public static FunctionClass fromFunctionName(String fnName) {

        if (datetime_funcs.contains(fnName.toLowerCase())) {
            return datetime;
        }

        return other;
    }

    private static ImmutableSet<String> datetime_funcs =
            ImmutableSet.of(
                    "add_months",
                    "current_date",
                    "current_timestamp",
                    "current_timezone",
                    "localtimestamp",
                    "datediff",
                    "date_diff",
                    "date_add",
                    "dateadd",
                    "date_format",
                    "date_sub",
                    "day",
                    "dayofyear",
                    "dayofmonth",
                    "from_unixtime",
                    "from_utc_timestamp",
                    "hour",
                    "last_day",
                    "minute",
                    "month",
                    "months_between",
                    "next_day",
                    "now",
                    "quarter",
                    "second",
                    "to_timestamp",
                    "to_date",
                    "to_binary",
                    "to_unix_timestamp",
                    "to_utc_timestamp",
                    "to_timestamp_ntz",
                    "to_timestamp_ltz",
                    "trunc",
                    "date_trunc",
                    "unix_timestamp",
                    "dayofweek",
                    "weekday",
                    "weekofyear",
                    "year",
                    "window",
                    "session_window",
                    "make_date",
                    "make_timestamp",
                    "make_timestamp_ntz",
                    "make_timestamp_ltz",
                    "make_interval",
                    "make_dt_interval",
                    "make_ym_interval",
                    "extract",
                    "date_part",
                    "datepart",
                    "date_from_unix_date",
                    "unix_date",
                    "timestamp_seconds",
                    "timestamp_millis",
                    "timestamp_micros",
                    "unix_seconds",
                    "unix_millis",
                    "unix_micros",
                    "convert_timezone"
            );
}
