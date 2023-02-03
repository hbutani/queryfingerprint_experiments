package org.hatke.queryfingerprint.model;

import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class Queryfingerprint implements Serializable {

    private static final long serialVersionUID = 4446331006878710519L;

    private final UUID hash;

    private final String sqlText;

    private Optional<UUID> parentQB;

    private final QBType type;

    private final boolean isCTE;

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
     * A list of fully-qualified-name of columns with group by (across all clauses)
     * in this Query Block or any referenced blocks(ctes, views) or sub-query blocks.
     */
    private final ImmutableSet<String> groupedColumns;

    /**
     * A list of fully-qualified-name of columns with order by (across all clauses)
     * in this Query Block or any referenced blocks(ctes, views) or sub-query blocks.
     */
    private final ImmutableSet<String> orderedColumns;


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


    public Queryfingerprint(String sqlText, boolean isCTE,
                            QBType type, ImmutableSet<String> tablesReferenced,
                            ImmutableSet<String> columnsScanned,
                            ImmutableSet<String> columnsFiltered,
                            ImmutableSet<String> columnsScanFiltered,
                            ImmutableSet<Predicate> predicates,
                            ImmutableSet<Predicate> scanPredicates,
                            ImmutableSet<FunctionApplication> functionApplications,
                            ImmutableSet<Join> joins,
                            ImmutableSet<String> correlatedColumns,
                            ImmutableSet<UUID> referencedQBlocks,
                            ImmutableSet<String> columnsGroupBy,
                            ImmutableSet<String> columnsOrderBy
    ) {
        this.sqlText = sqlText;
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
        this.isCTE = isCTE;
        this.groupedColumns = columnsGroupBy;
        this.orderedColumns = columnsOrderBy;
        this.hash = createUniqueHash();
        this.parentQB = Optional.empty();
    }

    private UUID createUniqueHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.type);
        sb.append(tablesReferenced.stream().sorted().reduce("", String::concat));
        sb.append(columnsScanned.stream().sorted().reduce("", String::concat));
        sb.append(columnsFiltered.stream().sorted().reduce("", String::concat));
        sb.append(columnsScanFiltered.stream().sorted().reduce("", String::concat));
        sb.append(predicates.stream().map(e -> e.toString()).sorted().reduce("", String::concat));
        sb.append(scanPredicates.stream().map(e -> e.toString()).sorted().reduce("", String::concat));
        sb.append(functionApplications.stream().map(e -> e.toString()).sorted().reduce("", String::concat));
        sb.append(joins.stream().map(e -> e.toString()).sorted().reduce("", String::concat));
        sb.append(correlatedColumns.stream().sorted().reduce("", String::concat));
        sb.append(groupedColumns.stream().sorted().reduce("", String::concat));
        sb.append(orderedColumns.stream().sorted().reduce("", String::concat));
        sb.append(isCTE);
        return UUID.nameUUIDFromBytes(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public UUID getHash() {
        return hash;
    }

    public String getSqlText() {
        return sqlText;
    }

    public Optional<UUID> getParentQB() {
        return parentQB;
    }

    public void setParentQB(UUID uuid) {
        this.parentQB = Optional.of(uuid);
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

    public boolean isCTE() {
        return isCTE;
    }

    public ImmutableSet<String> getGroupedColumns() {
        return groupedColumns;
    }

    public ImmutableSet<String> getOrderedColumns() {
        return orderedColumns;
    }

    @Override
    public String toString() {
        return "Queryfingerprint{" +
                "\n  hash=" + hash +
                "\n sqlText=" + sqlText +
                "\n isCTE=" + isCTE +
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
                "\n  groupedColumns=" + groupedColumns.stream().collect(Collectors.joining(", ", "[", "]")) +
                "\n  orderedColumns=" + orderedColumns.stream().collect(Collectors.joining(", ", "[", "]")) +
                "\n  correlatedColumns=" + correlatedColumns.stream().collect(Collectors.joining(", ", "[", "]")) +
                "\n  referencedQBlocks=" + referencedQBlocks.stream().map(f -> f.toString()).collect(Collectors.joining(", ", "[", "]")) +
                "\n}";
    }
}
