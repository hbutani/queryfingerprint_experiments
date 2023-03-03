package org.hatke.queryfingerprint.model;

import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
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

    private final Optional<QFPExplanation> explanation;


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
        this.explanation = Optional.empty();
    }

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
                            ImmutableSet<String> columnsOrderBy,
                            UUID hash,
                            Optional<UUID> parentQB,
                            Optional<QFPExplanation> explanation
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
        this.hash = hash;
        this.parentQB = parentQB;
        this.explanation = explanation;
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
        sb.append(referencedQBlocks.stream().map(e -> e.toString()).sorted().reduce("", String::concat));
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

    public int[] getFeatureVector() {
        return new int[]{
                tablesReferenced.size(),
                joins.size(),
                predicates.size(),
                groupedColumns.size(),
                functionApplications.stream().map(f -> f.isAggregate()).collect(Collectors.toList()).size()
        };
    }

    public Optional<QFPExplanation> getExplanation() {
        return explanation;
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

    public static class Builder {

        private final String sqlText;
        private final boolean isCTE;
        private final QBType qbType;

        private HashSet<String> tablesReferenced = new HashSet<>();
        private HashSet<String> columnsScanned = new HashSet<>();
        private HashSet<String> columnsFiltered = new HashSet<>();
        private HashSet<String> columnsScanFiltered = new HashSet<>();
        private HashSet<Predicate> predicates = new HashSet<>();
        private HashSet<Predicate> scanPredicates = new HashSet<>();
        private HashSet<FunctionApplication> functionApplications = new HashSet<>();
        private HashSet<Join> joins = new HashSet<>();
        private HashSet<String> correlatedColumns = new HashSet<>();
        private HashSet<UUID> referencedQBlocks = new HashSet<>();
        private HashSet<String> groupedColumns = new HashSet<>();
        private HashSet<String> orderedColumns = new HashSet<>();

        public Builder(String sqlText, boolean isCTE, QBType qbType) {
            this.sqlText = sqlText;
            this.isCTE = isCTE;
            this.qbType = qbType;
        }

        public void addTableReferenced(String t) {
            tablesReferenced.add(t);
        }

        public void addAllTableReferenced(ImmutableSet<String> s) {
            tablesReferenced.addAll(s);
        }

        public void addColumnScanned(String t) {
            columnsScanned.add(t);
        }

        public void addAllColumnsScanned(ImmutableSet<String> s) {
            columnsScanned.addAll(s);
        }

        public void addColumnFiltered(String t) {
            columnsFiltered.add(t);
        }

        public void addAllColumnsFiltered(ImmutableSet<String> s) {
            columnsFiltered.addAll(s);
        }

        public void addColumnScanFiltered(String t) {
            columnsScanFiltered.add(t);
        }

        public void addAllColumnsScanFiltered(ImmutableSet<String> s) {
            columnsScanFiltered.addAll(s);
        }

        public void addPredicate(Predicate p) {
            predicates.add(p);
        }

        public void addAllPredicates(ImmutableSet<Predicate> s) {
            predicates.addAll(s);
        }

        public void addScanPredicate(Predicate p) {
            scanPredicates.add(p);
        }

        public void addAllScanPredicates(ImmutableSet<Predicate> s) {
            scanPredicates.addAll(s);
        }

        public void addFunctionApplication(FunctionApplication f) {
            functionApplications.add(f);
        }

        public void addAllFunctionApplications(ImmutableSet<FunctionApplication> s) {
            functionApplications.addAll(s);
        }

        public void addJoin(Join f) {
            joins.add(f);
        }

        public void addAllJoins(ImmutableSet<Join> s) {
            joins.addAll(s);
        }

        public void addCorrelatedColumn(String c) {
            correlatedColumns.add(c);
        }

        public void addAllCorrelatedColumns(ImmutableSet<String> s) {
            correlatedColumns.addAll(s);
        }

        public void addReferencedQBlock(UUID u) {
            referencedQBlocks.add(u);
        }

        public void addAllReferencedQBlocks(ImmutableSet<UUID> s) {
            referencedQBlocks.addAll(s);
        }

        public void addGroupedColumn(String c) {
            groupedColumns.add(c);
        }

        public void addAllGroupedColumns(ImmutableSet<String> s) {
            groupedColumns.addAll(s);
        }

        public void addOrderedColumn(String c) {
            orderedColumns.add(c);
        }

        public void addAllOrderedColumns(ImmutableSet<String> s) {
            orderedColumns.addAll(s);
        }

        public void merge(Queryfingerprint childFP) {
            tablesReferenced.addAll(childFP.getTablesReferenced());
            columnsScanned.addAll(childFP.getColumnsScanned());
            columnsFiltered.addAll(childFP.getColumnsFiltered());
            columnsScanFiltered.addAll(childFP.getColumnsScanFiltered());
            predicates.addAll(childFP.getPredicates());
            scanPredicates.addAll(childFP.getScanPredicates());
            functionApplications.addAll(childFP.getFunctionApplications());
            joins.addAll(childFP.getJoins());
            referencedQBlocks.add(childFP.getHash());
            referencedQBlocks.addAll(childFP.getReferencedQBlocks());
            groupedColumns.addAll(childFP.getGroupedColumns());
            orderedColumns.addAll(childFP.getOrderedColumns());
        }

        public Queryfingerprint build() {
            return new Queryfingerprint(
                    sqlText,
                    isCTE,
                    qbType,
                    ImmutableSet.copyOf(tablesReferenced),
                    ImmutableSet.copyOf(columnsScanned),
                    ImmutableSet.copyOf(columnsFiltered),
                    ImmutableSet.copyOf(columnsScanFiltered),
                    ImmutableSet.copyOf(predicates),
                    ImmutableSet.copyOf(scanPredicates),
                    ImmutableSet.copyOf(functionApplications),
                    ImmutableSet.copyOf(joins),
                    ImmutableSet.copyOf(correlatedColumns),
                    ImmutableSet.copyOf(referencedQBlocks),
                    ImmutableSet.copyOf(groupedColumns),
                    ImmutableSet.copyOf(orderedColumns)
            );
        }
    }
}
