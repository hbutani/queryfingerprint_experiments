package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;
import org.hatke.queryfingerprint.model.FunctionApplication;
import org.hatke.queryfingerprint.model.Join;
import org.hatke.queryfingerprint.model.JoinType;
import org.hatke.queryfingerprint.model.Predicate;
import org.hatke.queryfingerprint.model.Queryfingerprint;
import org.hatke.queryfingerprint.model.Queryfingerprint.Builder;
import org.hatke.queryfingerprint.snowflake.parse.features.CorrelateJoinFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.FuncCallFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.JoinFeature;
import org.hatke.queryfingerprint.snowflake.parse.features.PredicateFeature;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class QueryfingerprintBuilder {

    private final QueryAnalysis qA;


    private Map<UUID, Integer> parentIdMap;
    private Map<Integer, Queryfingerprint> fpMap;
    private Map<UUID, Queryfingerprint> hashFpMap;

    public QueryfingerprintBuilder(QueryAnalysis qA) {
        this.qA = qA;
        fpMap = new HashMap<>();
        hashFpMap = new HashMap<>();
        parentIdMap = new HashMap<>();
    }


    public ImmutableList<Queryfingerprint> build() {
        build(qA.getTopLevelQB());
        updateParentHash();
        return ImmutableList.copyOf(fpMap.values());
    }

    private void updateParentHash() {
        parentIdMap.forEach((hash,id) ->  hashFpMap.get(hash).setParentQB(fpMap.get(id).getHash()) );
    }

    private Queryfingerprint build(QB qb) {

        Queryfingerprint qFP = fpMap.get(qb.getId());

        if (qFP == null) {
            Builder qbBuilder = createQBuilder(qb);

            for(QB childQB : qb.childQBs()) {
                Queryfingerprint childFP = build(childQB);
                qbBuilder.merge(childFP);
            }

            for(QB cteQB : qb.cteRefs()) {
                Queryfingerprint childFP = build(cteQB);
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
            hashFpMap.put(qFP.getHash(), qFP);
            if(qb.getParentQB().isPresent()) {
                parentIdMap.put(qFP.getHash(), qb.getParentQB().get().getId());
            }
        }

       return qFP;
    }

    private Builder createQBuilder(QB qb) {
//        assert uuidMap.containsKey(qb.getId());
//        assert qb.getParentQB().isEmpty() || uuidMap.containsKey(qb.getParentQB().get().getId());

        Builder qbBuilder = new Builder(qb.getSelectStat().toString(),
                qb.isCTE(),
                qb.getQbType());

        if (qb instanceof SingleQB) {
            addFeatures((SingleQB) qb, qbBuilder);
        }

        return qbBuilder;
    }

    private void addJoinFeature(Optional<Column> leftCatCol,
                                Optional<Column> rightCatCol,
                                JoinType joinType,
                                Builder qbBuilder) {
        if (leftCatCol.isPresent() && rightCatCol.isPresent()) {

            CatalogColumn leftCol = (CatalogColumn) leftCatCol.get();
            CatalogTable leftTab = leftCol.getTable();
            CatalogColumn rightCol = (CatalogColumn) rightCatCol.get();
            CatalogTable rightTab = rightCol.getTable();


            qbBuilder.addJoin(
                    new Join(leftTab.getFqName(), rightTab.getFqName(), leftCol.getFQN(), rightCol.getFQN(), joinType)
            );
        }
    }

    private void addFeatures(SingleQB qb, Builder qbBuilder) {
        for(Source src : qb.getFromSources()) {
            src.asCatalogTable().stream().forEach(c -> qbBuilder.addTableReferenced(c.getFqName()));
        }

        for(Column c : qb.getScannedColumns()) {
            c.asCatalogColumn().stream().forEach(sc -> qbBuilder.addColumnScanned(sc.getFQN()));
        }

        for(Column c : qb.getFilteredColumns()) {
            c.asCatalogColumn().stream().forEach(sc -> qbBuilder.addColumnFiltered(sc.getFQN()));
        }

        for(FuncCallFeature fC : qb.functionApplications()) {
            for(ColumnRef cR : fC.getColumnRefs()) {
                cR.getColumn().asCatalogColumn().stream().forEach(sc -> {
                    qbBuilder.addFunctionApplication(new FunctionApplication(fC.getFuncName(), sc.getFQN()));
                });
            }
        }

        for(PredicateFeature p : qb.prunablePredicates()) {
            for(ColumnRef cR : p.getColumnRefs()) {
                cR.getColumn().asCatalogColumn().stream().forEach(sc -> {
                    qbBuilder.addColumnScanFiltered(sc.getFQN());

                    Optional<FuncCallFeature> fC = p.getFuncCalls().size() == 0 ?
                            Optional.empty() : Optional.of(p.getFuncCalls().get(0));

                    qbBuilder.addScanPredicate(
                            new Predicate(fC.map(f -> f.getFuncName()),
                                    sc.getFQN(), p.getComparisonType().name(),
                                    Optional.of(p.getValueFeature().getContantValue())
                            )
                    );
                });
            }
        }

        for(JoinFeature jF : qb.joins()) {

            Optional<Column> leftCatCol = Optional.empty(), rightCatCol = Optional.empty();

            for(ColumnRef cR : jF.getLeftFeature().getColumnRefs()) {
                leftCatCol = cR.getColumn().asCatalogColumn();
            }

            for(ColumnRef cR : jF.getRightFeature().getColumnRefs()) {
                rightCatCol = cR.getColumn().asCatalogColumn();
            }

            addJoinFeature(leftCatCol, rightCatCol, jF.getJoinType(), qbBuilder);
        }

        for(CorrelateJoinFeature cJF : qb.correlatedJoins()) {
            addJoinFeature(cJF.getColRef().getColumn().asCatalogColumn(),
                    cJF.getChildColRef().getColumn().asCatalogColumn(), JoinType.inner, qbBuilder);
        }

        for(Column c : qb.columnsFromParent()) {
            c.asCatalogColumn().stream().forEach(sc -> qbBuilder.addCorrelatedColumn(sc.getFQN()));
        }

        for(Column c : qb.getGroupedColumns()) {
            c.asCatalogColumn().stream().forEach(sc -> qbBuilder.addGroupedColumn(sc.getFQN()));
        }

        for(Column c : qb.getOrderedColumns()) {
            c.asCatalogColumn().stream().forEach(sc -> qbBuilder.addOrderedColumn(sc.getFQN()));
        }
    }
}
