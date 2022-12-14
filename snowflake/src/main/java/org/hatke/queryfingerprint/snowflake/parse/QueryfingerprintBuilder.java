package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hatke.queryfingerprint.model.FunctionApplication;
import org.hatke.queryfingerprint.model.Join;
import org.hatke.queryfingerprint.model.Predicate;
import org.hatke.queryfingerprint.model.Queryfingerprint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class QueryfingerprintBuilder {

    private final QueryAnalysis qA;


    private Map<Integer, UUID> uuidMap;
    private Map<Integer, Queryfingerprint> fpMap;

    QueryfingerprintBuilder(QueryAnalysis qA) {
        this.qA = qA;
        uuidMap = new HashMap<>();
        fpMap = new HashMap<>();
        assignUUid(qA.getTopLevelQB());
        for(SourceRef sR : qA.ctes().values()) {
            assignUUid(sR);
        }
    }

    private void assignUUid(Source src) {
        if (src instanceof SourceRef) {
            assignUUid(((SourceRef) src).getSource());
        } else if (src instanceof QB) {
            QB qb = (QB) src;
            if (!uuidMap.containsKey(qb.getId())) {
                uuidMap.put(qb.getId(), UUID.randomUUID());
                for (QB cQB : qb.childQBs()) {
                    assignUUid(cQB);
                }
            }
        }
    }

    ImmutableList<Queryfingerprint> build() {
        build(qA.getTopLevelQB());
        return ImmutableList.copyOf(fpMap.values());
    }

    private Queryfingerprint build(QB qb) {

        Queryfingerprint qFP = fpMap.get(qb.getId());

        if (qFP == null) {
            QBBuilder qbBuilder = createQBuilder(qb);

            for(QB childQB : qb.childQBs()) {
                Queryfingerprint childFP = build(childQB);
                qbBuilder.merge(childFP);
            }

            if (qb.isTopLevel()) {
                for(SourceRef sR : qA.ctes().values()) {
                    Source src = sR.getSource();
                    if (src instanceof QB) {
                        Queryfingerprint childFP = build((QB) src);
                        qbBuilder.merge(childFP);
                    }
                }
            }
            qFP = qbBuilder.build();
            fpMap.put(qb.getId(), qFP);
        }

       return qFP;
    }

    private QBBuilder createQBuilder(QB qb) {
        assert uuidMap.containsKey(qb.getId());
        assert qb.getParentQB().isEmpty() || uuidMap.containsKey(qb.getParentQB().get().getId());

        QBBuilder qbBuilder = new QBBuilder(qb);

        if (qb instanceof SingleQB) {
            addFeatures((SingleQB) qb, qbBuilder);
        }

        return qbBuilder;
    }

    private void addFeatures(SingleQB qb, QBBuilder qbBuilder) {
        for(Source src : qb.getFromSources()) {
            if (src instanceof CatalogTable) {
                qbBuilder.tablesReferenced.add(((CatalogTable) src).getFqName());
            }
        }

        for(Column c : qb.getScannedColumns()) {
            qbBuilder.columnsScanned.add(c.getFQN());
        }
    }

    class QBBuilder {

        final QB qb;

        final UUID id;

        final Optional<UUID> parentId;

        HashSet<String> tablesReferenced = new HashSet<>();
        HashSet<String> columnsScanned = new HashSet<>();
        HashSet<String> columnsFiltered = new HashSet<>();
        HashSet<String> columnsScanFiltered = new HashSet<>();
        HashSet<Predicate> predicates = new HashSet<>();
        HashSet<Predicate> scanPredicates = new HashSet<>();
        HashSet<FunctionApplication> functionApplications = new HashSet<>();
        HashSet<Join> joins = new HashSet<>();
        HashSet<String> correlatedColumns = new HashSet<>();
        HashSet<UUID> referencedQBlocks = new HashSet<>();

        public QBBuilder(QB qb) {
            this.qb = qb;
            this.id = QueryfingerprintBuilder.this.uuidMap.get(qb.getId());
            this.parentId = qb.getParentQB().map(p -> QueryfingerprintBuilder.this.uuidMap.get(p.getId()));
        }

        void merge(Queryfingerprint childFP) {
            tablesReferenced.addAll(childFP.getTablesReferenced());
            columnsScanned.addAll(childFP.getColumnsScanned());
            columnsFiltered.addAll(childFP.getColumnsFiltered());
            columnsScanFiltered.addAll(childFP.getColumnsScanFiltered());
            predicates.addAll(childFP.getPredicates());
            scanPredicates.addAll(childFP.getScanPredicates());
            functionApplications.addAll(childFP.getFunctionApplications());
            joins.addAll(childFP.getJoins());
            correlatedColumns.addAll(childFP.getCorrelatedColumns());
            referencedQBlocks.add(childFP.getUuid());
            referencedQBlocks.addAll(childFP.getReferencedQBlocks());
        }

        Queryfingerprint build() {
            return new Queryfingerprint(
                    id,
                    parentId,
                    qb.getQbType(),
                    ImmutableSet.copyOf(tablesReferenced),
                    ImmutableSet.copyOf(columnsScanned),
                    ImmutableSet.copyOf(columnsFiltered),
                    ImmutableSet.copyOf(columnsScanned),
                    ImmutableSet.copyOf(predicates),
                    ImmutableSet.copyOf(scanPredicates),
                    ImmutableSet.copyOf(functionApplications),
                    ImmutableSet.copyOf(joins),
                    ImmutableSet.copyOf(correlatedColumns),
                    ImmutableSet.copyOf(referencedQBlocks)
            );
        }

    }
}
