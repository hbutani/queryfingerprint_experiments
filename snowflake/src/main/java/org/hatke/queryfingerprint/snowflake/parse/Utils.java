package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TResultColumn;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.sqlenv.ESQLDataObjectType;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import gudusoft.gsqlparser.util.SQLUtil;
import org.hatke.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {

    public static boolean isEmpty(String value) {
        return SQLUtil.isEmpty(value);
    }

    public static String stringValue(TObjectName objName, ESQLDataObjectType objectType) {
        return objName == null ? null : objName.toString();
    }

    /**
     * copied from {@link SQLUtil}
     * @param dbVendor
     * @param objectType
     * @param identifier
     * @return
     */
    private static String normalizeIdentifier(EDbVendor dbVendor, ESQLDataObjectType objectType, String identifier) {
        if (identifier == null) {
            return null;
        } else {
            identifier = TSQLEnv.normalizeIdentifier(dbVendor, objectType, identifier);
            boolean collationSensitive = false;
            switch (objectType) {
                case dotCatalog:
                case dotSchema:
                case dotFunction:
                case dotProcedure:
                case dotTrigger:
                case dotTable:
                    collationSensitive = TSQLEnv.tableCollationCaseSensitive[dbVendor.ordinal()];
                    break;
                case dotColumn:
                    collationSensitive = TSQLEnv.columnCollationCaseSensitive[dbVendor.ordinal()];
                    break;
                default:
                    collationSensitive = TSQLEnv.defaultCollationCaseSensitive[dbVendor.ordinal()];
            }

            return collationSensitive ? identifier : identifier.toUpperCase();
        }
    }

    /**
     * based on {@link SQLUtil:normalizedName} and functions in {@link gudusoft.gsqlparser.dlineage.util.DlineageUtil}
     *
     * Behavior is to normalize components of give objName based on the dbVendor of the SQLEnv.
     * Also return the unqualified normalized name.
     */
    public static Pair<String, String> normalizedName(TSQLEnv sqlEnv,
                                                      TObjectName objName,
                                                      ESQLDataObjectType sqlDataObjectType) {
        String name = stringValue(objName, sqlDataObjectType);

        if (name == null) {
            return null;
        } else {
            EDbVendor dbVendor = sqlEnv.getDBVendor();
            List<String> segments = SQLUtil.parseNames(name, sqlEnv.getDBVendor());
            int numElems = segments.size();
            if (sqlDataObjectType == ESQLDataObjectType.dotColumn) {
                if (numElems >= 4) {
                    segments.set(numElems - 4,
                            normalizeIdentifier(dbVendor, ESQLDataObjectType.dotCatalog, segments.get(numElems - 4))
                    );
                }
                if (numElems >= 3) {
                    segments.set(numElems - 3,
                            normalizeIdentifier(dbVendor, ESQLDataObjectType.dotSchema, segments.get(numElems - 3)));
                }
                if (numElems >= 2) {
                    segments.set(numElems - 2,
                            normalizeIdentifier(dbVendor, ESQLDataObjectType.dotTable, segments.get(numElems - 2)));
                }
                segments.set(numElems - 1,
                        normalizeIdentifier(dbVendor, ESQLDataObjectType.dotColumn, segments.get(numElems - 1)));
            } else if (sqlDataObjectType != ESQLDataObjectType.dotTable && sqlDataObjectType != ESQLDataObjectType.dotFunction && sqlDataObjectType != ESQLDataObjectType.dotProcedure && sqlDataObjectType != ESQLDataObjectType.dotTrigger) {
                if (sqlDataObjectType == ESQLDataObjectType.dotSchema) {
                    if (numElems >= 2) {
                        segments.set(numElems - 2,
                                normalizeIdentifier(dbVendor, ESQLDataObjectType.dotCatalog, segments.get(numElems - 2)));
                    }
                    segments.set(numElems - 1,
                                normalizeIdentifier(dbVendor, sqlDataObjectType, segments.get(numElems - 1)));
                } else if (sqlDataObjectType == ESQLDataObjectType.dotCatalog) {
                    segments.set(numElems - 1,
                            normalizeIdentifier(dbVendor, sqlDataObjectType, segments.get(numElems - 1)));
                }
            } else {

                if (numElems >= 3) {
                    segments.set(numElems - 3,
                            normalizeIdentifier(dbVendor, ESQLDataObjectType.dotCatalog, segments.get(numElems - 3))
                    );
                }
                if (numElems >= 2) {
                    segments.set(numElems - 2,
                            normalizeIdentifier(dbVendor, ESQLDataObjectType.dotSchema, segments.get(numElems - 2)));
                }
                segments.set(numElems - 1,
                        normalizeIdentifier(dbVendor, ESQLDataObjectType.dotColumn, segments.get(numElems - 1)));
            }

            String normalizeNm = segments.stream().collect(Collectors.joining("."));

            return Pair.pairOf(segments.get(segments.size() - 1), normalizeNm);
        }

    }

    public static String qualifiedName(String... names) {
        StringBuilder b = new StringBuilder();
        boolean empty = true;
        for(String nm : names) {
            if (nm != null) {
                if (!empty) {
                    b.append(".");
                }
                b.append(nm);
                empty = false;
            }
        }
        return  b.toString();
    }

    public static Pair<String, String> fqNormalizedTableName(TSQLEnv sqlEnv,
                                                             TObjectName objName) {
        String tableName = stringValue(objName, ESQLDataObjectType.dotTable);
        return fqNormalizedTableName(sqlEnv, tableName);
    }

    public static Pair<String, String> fqNormalizedTableName(TSQLEnv sqlEnv,
                                                             String tableName) {
        List<String> segments = SQLUtil.parseNames(tableName);
        int numElems = segments.size();
        EDbVendor dbVendor = sqlEnv.getDBVendor();
        String defaultSchema = sqlEnv.getDefaultSchemaName();
        String defaultDB = defaultSchema == null ? null : sqlEnv.getDefaultCatalogName();

        String tableDB = numElems > 3 ? segments.get(numElems - 3) : defaultDB;
        tableDB = tableDB != null ? normalizeIdentifier(dbVendor, ESQLDataObjectType.dotCatalog, tableDB) : null;

        String tableSchema = numElems > 2 ? segments.get(numElems - 2) : defaultSchema;
        tableSchema = tableSchema != null ? normalizeIdentifier(dbVendor, ESQLDataObjectType.dotSchema, tableSchema) : null;

        String tabName = segments.get(numElems - 1);
        tabName = normalizeIdentifier(dbVendor, ESQLDataObjectType.dotSchema, tabName);

        return Pair.pairOf(tabName, qualifiedName(tableDB, tableSchema, tabName));
    }

    public static List<String> normalizedColName(TSQLEnv sqlEnv,
                                                 TObjectName objName) {

        String colName = stringValue(objName, ESQLDataObjectType.dotColumn);
        List<String> segments = SQLUtil.parseNames(colName);
        int numElems = segments.size();
        EDbVendor dbVendor = sqlEnv.getDBVendor();

        if (numElems > 1) {
            segments.set(numElems - 2,
                    normalizeIdentifier(dbVendor, ESQLDataObjectType.dotTable, segments.get(numElems - 2))
            );
        }

        segments.set(numElems - 1,
                normalizeIdentifier(dbVendor, ESQLDataObjectType.dotColumn, segments.get(numElems - 1))
        );

        return segments;

    }



    public static String colAttr(TResultColumn col, Function<TResultColumn, String> extractAttr) {
        String v = extractAttr.apply(col);
        return v == null || v == "" ? null : v;
    }

    public static ImmutableList<String> outShape(TSelectSqlStatement selectStat) {

        ImmutableList.Builder<String> outputShapeBldr = new ImmutableList.Builder();

        for(TResultColumn col : selectStat.getResultColumnList()) {
            String outName = colAttr(col, TResultColumn::getColumnAlias);
            outName = outName == null ? colAttr(col, TResultColumn::getColumnNameOnly) : outName;
            outName = outName == null ? col.getExpr().toString() : outName;
           outputShapeBldr.add(outName);

        }
        return outputShapeBldr.build();
    }

    public static String fqn(TTable table) {
        String schema = table.getTableName().getSchemaString();
        schema = (schema == null || schema.equals("")) ?
                table.getSqlEnv().getDefaultSchemaName() : schema;

        String namespace = table.getTableName().getNamespace() != null ?
                table.getTableName().getNamespace().toString() :
                table.getSqlEnv().getDefaultCatalogName();

        if (namespace != null && schema != null) {
            return String.format("%1$s.%2$s.%3$s", namespace, schema, table.getName());
        } else if (schema != null) {
            return String.format("%1$s.%2$s", schema, table.getName());
        } else {
            return table.getName();
        }
    }

    public static Logger getLogger() {
        return LoggerFactory.getLogger(QueryAnalysis.class.getName());
    }
}
