package org.hatke.queryfingerprint.snowflake.parse;

import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;

import java.util.Optional;

public interface Source {

    int getId();

    Optional<Column> resolveColumn(TObjectName objName);

    TSQLEnv getSqlEnv();

    Iterable<Column> columns();
}
