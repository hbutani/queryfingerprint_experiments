package org.hatke.queryfingerprint.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class Queryfingerprint implements Serializable  {

    private static final long serialVersionUID = 4446331006878710519L;

    private final UUID uuid;

    private final String sqlText;

    private final Optional<UUID> parentQB;

    private final QBType type;

    /**
     * A list of fully-qualified-name of tables referenced in this Query Block or any
     * referenced blocks(ctes, views) or sub-query blocks.
     */
    private final ImmutableSet<String> tablesReferenced;

    /**
     * A list of fully-qualified-name of columns referenced(across all clauses)
     * in this Query Block or any referenced blocks(ctes, views) or sub-query blocks.
     */
    private final ImmutableSet<String> columnsScanned;

    /**
     * A list of fully-qualified-name of columns with predicates(across all clauses)
     * in this Query Block or any referenced blocks(ctes, views) or sub-query blocks.
     */
    private final ImmutableSet<String> columnsFiltered;

    /**
     * A list of fully-qualified-name of columns with predicates(across all clauses)
     * that can be pushed to their table scan operation,
     * in this Query Block or any referenced blocks(ctes, views) or sub-query blocks.
     */
    private final ImmutableSet<String> columnsScanFiltered;

    /**
     * List of {@link Predicate} in this Query Block or referenced or child query block.
     */
    private final ImmutableSet<Predicate> predicates;

    /**
     * List of {@link Predicate} in this Query Block or referenced or child query block
     * that can be pushed to their table scan operation.
     */
    private final ImmutableSet<Predicate> scanPredicates;

    /**
     * Functions application on a column in any clause
     * in this Query Block or any referenced blocks(ctes, views) or sub-query blocks.
     */
    private final ImmutableSet<FunctionApplication> functionApplications;

    /**
     * Joins in this Query block or any referenced blocks(ctes, views) or sub-query blocks.
     */
    private final ImmutableSet<Join> joins;

    /**
     * For a correlated Query Block the parent columns refernced in this Block.
     */
    private final ImmutableSet<String> correlatedColumns;


    /**
     * Query Blocks referenced or appearing in this Query Block.
     */
    private final ImmutableSet<UUID> referencedQBlocks;


    public Queryfingerprint(UUID uuid, String sqlText,
                            Optional<UUID> parentQB, QBType type, ImmutableSet<String> tablesReferenced,
                            ImmutableSet<String> columnsScanned,
                            ImmutableSet<String> columnsFiltered,
                            ImmutableSet<String> columnsScanFiltered,
                            ImmutableSet<Predicate> predicates,
                            ImmutableSet<Predicate> scanPredicates,
                            ImmutableSet<FunctionApplication> functionApplications,
                            ImmutableSet<Join> joins,
                            ImmutableSet<String> correlatedColumns,
                            ImmutableSet<UUID> referencedQBlocks) {
        this.uuid = uuid;
        this.sqlText = sqlText;
        this.parentQB = parentQB;
        this.type = type;
        this.tablesReferenced = tablesReferenced;
        this.columnsScanned = columnsScanned;
        this.columnsFiltered = columnsFiltered;
        this.columnsScanFiltered = columnsScanFiltered;
        this.predicates = predicates;
        this.scanPredicates = scanPredicates;
        this.functionApplications = functionApplications;
        this.joins = joins;
        this.correlatedColumns = correlatedColumns;
        this.referencedQBlocks = referencedQBlocks;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Optional<UUID> getParentQB() {
        return parentQB;
    }

    public QBType getType() {
        return type;
    }

    public ImmutableSet<String> getTablesReferenced() {
        return tablesReferenced;
    }

    public ImmutableSet<String> getColumnsScanned() {
        return columnsScanned;
    }

    public ImmutableSet<String> getColumnsFiltered() {
        return columnsFiltered;
    }

    public ImmutableSet<String> getColumnsScanFiltered() {
        return columnsScanFiltered;
    }

    public ImmutableSet<Predicate> getPredicates() {
        return predicates;
    }

    public ImmutableSet<Predicate> getScanPredicates() {
        return scanPredicates;
    }

    public ImmutableSet<FunctionApplication> getFunctionApplications() {
        return functionApplications;
    }

    public ImmutableSet<Join> getJoins() {
        return joins;
    }

    public ImmutableSet<String> getCorrelatedColumns() {
        return correlatedColumns;
    }

    public ImmutableSet<UUID> getReferencedQBlocks() {
        return referencedQBlocks;
    }

    @Override
    public String toString() {
        return "Queryfingerprint{" +
                "\n  uuid=" + uuid +
                "\n sqlText=" + sqlText +
                Utils.optionalInfoString("\n  parentQB", parentQB) +
                "\n  type=" + type +
                "\n  tablesReferenced=" + tablesReferenced.stream().collect(Collectors.joining(", ", "[", "]")) +
                "\n  columnsScanned=" + columnsScanned.stream().collect(Collectors.joining(", ", "[", "]")) +
                "\n  columnsFiltered=" + columnsFiltered.stream().collect(Collectors.joining(", ", "[", "]")) +
                "\n  columnsScanFiltered=" + columnsScanFiltered.stream().collect(Collectors.joining(", ", "[", "]")) +
                "\n  predicates=" + predicates.stream().map(p -> p.toString()).collect(Collectors.joining(", ", "[", "]")) +
                "\n  scanPredicates=" + scanPredicates.stream().map(p -> p.toString()).collect(Collectors.joining(", ", "[", "]")) +
                "\n  functionApplications=" + functionApplications.stream().map(f -> f.toString()).collect(Collectors.joining(", ", "[", "]")) +
                "\n  joins=" + joins.stream().map(f -> f.toString()).collect(Collectors.joining(", ", "[", "]")) +
                "\n  correlatedColumns=" + correlatedColumns.stream().collect(Collectors.joining(", ", "[", "]")) +
                "\n  referencedQBlocks=" + referencedQBlocks.stream().map(f -> f.toString()).collect(Collectors.joining(", ", "[", "]")) +
                "\n}";
    }
}
