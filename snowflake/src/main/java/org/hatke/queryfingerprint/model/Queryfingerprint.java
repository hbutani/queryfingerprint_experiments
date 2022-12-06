package org.hatke.queryfingerprint.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.Optional;

public class Queryfingerprint implements Serializable, Normalizable<Queryfingerprint> {

    private static final long serialVersionUID = -4723521893270777561L;

    private final Query query;

    private final ImmutableList<TableSource> tableSources;
    private final ImmutableMap<Integer, Optional<String>> tableSourcePosToAlias;

    private final ImmutableList<ColumnName> projectedColumns;

    private final ImmutableList<FunctionApplication> projectedFunctionApplications;

    private final ImmutableList<String> projectedExprs;

    private final ImmutableList<ColumnName> filteredColumns;

    private final ImmutableList<FunctionApplication> filteredFunctionApplications;

    private final ImmutableList<String> filteredExprs;

    private final ImmutableList<Join> joins;

    private final Group.Type groupType;

    private final ImmutableList<ColumnName> groupingColumns;
    private final ImmutableList<FunctionApplication> groupingFunctionApplications;
    private final ImmutableList<String> groupingExprs;

    private Queryfingerprint(Query query, ImmutableList<TableSource> tableSources,
                             ImmutableMap<Integer, Optional<String>> tableSourcePosToAlias,
                             ImmutableList<ColumnName> projectedColumns,
                             ImmutableList<FunctionApplication> projectedFunctionApplications,
                             ImmutableList<String> projectedExprs,
                             ImmutableList<ColumnName> filteredColumns,
                             ImmutableList<FunctionApplication> filteredFunctionApplications,
                             ImmutableList<String> filteredExprs, ImmutableList<Join> joins,
                             Group.Type groupType, ImmutableList<ColumnName> groupingColumns,
                             ImmutableList<FunctionApplication> groupingFunctionApplications,
                             ImmutableList<String> groupingExprs) {
        this.query = query;
        this.tableSources = tableSources;
        this.tableSourcePosToAlias = tableSourcePosToAlias;
        this.projectedColumns = projectedColumns;
        this.projectedFunctionApplications = projectedFunctionApplications;
        this.projectedExprs = projectedExprs;
        this.filteredColumns = filteredColumns;
        this.filteredFunctionApplications = filteredFunctionApplications;
        this.filteredExprs = filteredExprs;
        this.joins = joins;
        this.groupType = groupType;
        this.groupingColumns = groupingColumns;
        this.groupingFunctionApplications = groupingFunctionApplications;
        this.groupingExprs = groupingExprs;
    }

    @Override
    public Queryfingerprint normalize() {
        // TODO
        return this;
    }
}
