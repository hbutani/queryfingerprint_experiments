package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableMap;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.TStatementList;
import gudusoft.gsqlparser.nodes.*;
import gudusoft.gsqlparser.nodes.ENodeType;
import gudusoft.gsqlparser.nodes.TAlterTableOption;
import gudusoft.gsqlparser.nodes.TAnalyticFunction;
import gudusoft.gsqlparser.nodes.TBlockSqlNode;
import gudusoft.gsqlparser.nodes.TCTE;
import gudusoft.gsqlparser.nodes.TCTEList;
import gudusoft.gsqlparser.nodes.TCaseExpression;
import gudusoft.gsqlparser.nodes.TClusterBy;
import gudusoft.gsqlparser.nodes.TCompoundDmlTriggerClause;
import gudusoft.gsqlparser.nodes.TConnectByClause;
import gudusoft.gsqlparser.nodes.TConstant;
import gudusoft.gsqlparser.nodes.TConstantList;
import gudusoft.gsqlparser.nodes.TDatabaseEventItem;
import gudusoft.gsqlparser.nodes.TDdlEventItem;
import gudusoft.gsqlparser.nodes.TDmlEventItem;
import gudusoft.gsqlparser.nodes.TDropIndexItem;
import gudusoft.gsqlparser.nodes.TDropIndexItemList;
import gudusoft.gsqlparser.nodes.TExecParameter;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TExpressionList;
import gudusoft.gsqlparser.nodes.TGroupingExpressionItem;
import gudusoft.gsqlparser.nodes.TGroupingExpressionItemList;
import gudusoft.gsqlparser.nodes.TInExpr;
import gudusoft.gsqlparser.nodes.TInsertCondition;
import gudusoft.gsqlparser.nodes.TInsertIntoValue;
import gudusoft.gsqlparser.nodes.TIntoClause;
import gudusoft.gsqlparser.nodes.TJoin;
import gudusoft.gsqlparser.nodes.TJoinItem;
import gudusoft.gsqlparser.nodes.TJoinItemList;
import gudusoft.gsqlparser.nodes.TJoinList;
import gudusoft.gsqlparser.nodes.TKeepDenseRankClause;
import gudusoft.gsqlparser.nodes.TMergeDeleteClause;
import gudusoft.gsqlparser.nodes.TMySQLCreateTableOption;
import gudusoft.gsqlparser.nodes.TNonDmlTriggerClause;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TObjectNameList;
import gudusoft.gsqlparser.nodes.TParseTreeNode;
import gudusoft.gsqlparser.nodes.TParseTreeNodeList;
import gudusoft.gsqlparser.nodes.TPartitionClause;
import gudusoft.gsqlparser.nodes.TPivotClause;
import gudusoft.gsqlparser.nodes.TPivotInClause;
import gudusoft.gsqlparser.nodes.TPivotedTable;
import gudusoft.gsqlparser.nodes.TResultColumn;
import gudusoft.gsqlparser.nodes.TResultColumnList;
import gudusoft.gsqlparser.nodes.TSelectModifier;
import gudusoft.gsqlparser.nodes.TSimpleDmlTriggerClause;
import gudusoft.gsqlparser.nodes.TSubscripts;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.nodes.TTableElement;
import gudusoft.gsqlparser.nodes.TTableSample;
import gudusoft.gsqlparser.nodes.TTopClause;
import gudusoft.gsqlparser.nodes.TTriggeringClause;
import gudusoft.gsqlparser.nodes.TUnnestClause;
import gudusoft.gsqlparser.nodes.TUnpivotInClause;
import gudusoft.gsqlparser.nodes.TUnpivotInClauseItem;
import gudusoft.gsqlparser.nodes.TWhenClauseItem;
import gudusoft.gsqlparser.nodes.TWhenClauseItemList;
import gudusoft.gsqlparser.nodes.TWhereClause;
import gudusoft.gsqlparser.nodes.TWindowClause;
import gudusoft.gsqlparser.nodes.TWindowDef;
import gudusoft.gsqlparser.nodes.TWindowDefinition;
import gudusoft.gsqlparser.nodes.TWindowFrame;
import gudusoft.gsqlparser.nodes.TWindowFrameBoundary;
import gudusoft.gsqlparser.nodes.TWindowPartitioningSpec;
import gudusoft.gsqlparser.nodes.TWindowSpecification;
import gudusoft.gsqlparser.nodes.couchbase.TArrayConstruct;
import gudusoft.gsqlparser.nodes.couchbase.TBinding;
import gudusoft.gsqlparser.nodes.couchbase.TCollectionArray;
import gudusoft.gsqlparser.nodes.couchbase.TCollectionCondition;
import gudusoft.gsqlparser.nodes.couchbase.TCollectionFirst;
import gudusoft.gsqlparser.nodes.couchbase.TCollectionObject;
import gudusoft.gsqlparser.nodes.couchbase.TIndexRef;
import gudusoft.gsqlparser.nodes.couchbase.TNamedParameter;
import gudusoft.gsqlparser.nodes.couchbase.TObjectConstruct;
import gudusoft.gsqlparser.nodes.couchbase.TPair;
import gudusoft.gsqlparser.nodes.couchbase.TPositionalParameter;
import gudusoft.gsqlparser.nodes.couchbase.TUpdateFor;
import gudusoft.gsqlparser.nodes.couchbase.TUseKeyIndex;
import gudusoft.gsqlparser.nodes.dax.TDaxAddMissingItems;
import gudusoft.gsqlparser.nodes.dax.TDaxDatatable;
import gudusoft.gsqlparser.nodes.dax.TDaxExprPair;
import gudusoft.gsqlparser.nodes.dax.TDaxFunction;
import gudusoft.gsqlparser.nodes.dax.TDaxGroupBy;
import gudusoft.gsqlparser.nodes.dax.TDaxIsOnOrAfter;
import gudusoft.gsqlparser.nodes.dax.TDaxReturn;
import gudusoft.gsqlparser.nodes.dax.TDaxSubstituteWithIndex;
import gudusoft.gsqlparser.nodes.dax.TDaxSummarize;
import gudusoft.gsqlparser.nodes.dax.TDaxSummerizeColumns;
import gudusoft.gsqlparser.nodes.dax.TDaxVar;
import gudusoft.gsqlparser.nodes.hive.THiveDescTablePartition;
import gudusoft.gsqlparser.nodes.hive.THiveHintClause;
import gudusoft.gsqlparser.nodes.hive.THiveHintItem;
import gudusoft.gsqlparser.nodes.hive.THiveIndexProperties;
import gudusoft.gsqlparser.nodes.hive.THiveKeyValueProperty;
import gudusoft.gsqlparser.nodes.hive.THivePartitionedTableFunction;
import gudusoft.gsqlparser.nodes.hive.THivePrincipalName;
import gudusoft.gsqlparser.nodes.hive.THivePrivilegeDef;
import gudusoft.gsqlparser.nodes.hive.THiveRecordReader;
import gudusoft.gsqlparser.nodes.hive.THiveRecordWriter;
import gudusoft.gsqlparser.nodes.hive.THiveRowFormat;
import gudusoft.gsqlparser.nodes.hive.THiveTableBuckets;
import gudusoft.gsqlparser.nodes.hive.THiveTableFileFormat;
import gudusoft.gsqlparser.nodes.hive.THiveTablePartition;
import gudusoft.gsqlparser.nodes.hive.THiveTableProperties;
import gudusoft.gsqlparser.nodes.hive.THiveTableSkewed;
import gudusoft.gsqlparser.nodes.hive.THiveTerminatedIdentifier;
import gudusoft.gsqlparser.nodes.hive.THiveTransformClause;
import gudusoft.gsqlparser.nodes.hive.THiveVariable;
import gudusoft.gsqlparser.nodes.hive.THiveWithDBPropertiesClause;
import gudusoft.gsqlparser.nodes.mdx.TMdxAxisNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxBinOpNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxCalcPropNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxCaseNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxDimContentNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxDimensionNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxEmptyNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxExpNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxFloatConstNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxFunctionNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxGroupNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxIdentifierNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxIntegerConstNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxLevelContentNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxLevelNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxMeasureNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxMemberNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxNonEmptyNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxPropertyNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxSetNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxStringConstNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxTupleNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxUnaryOpNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxWhenNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxWhereNode;
import gudusoft.gsqlparser.nodes.mdx.TMdxWithNode;
import gudusoft.gsqlparser.nodes.mssql.TExecuteAsClause;
import gudusoft.gsqlparser.nodes.mssql.TMssqlCreateTriggerUpdateColumn;
import gudusoft.gsqlparser.nodes.mssql.TOptionClause;
import gudusoft.gsqlparser.nodes.mssql.TProcedureOption;
import gudusoft.gsqlparser.nodes.mysql.TGroupConcatParam;
import gudusoft.gsqlparser.nodes.netezza.TModeChoice;
import gudusoft.gsqlparser.nodes.netezza.TReclaimChoice;
import gudusoft.gsqlparser.nodes.oracle.TErrorLoggingClause;
import gudusoft.gsqlparser.nodes.oracle.TInvokerRightsClause;
import gudusoft.gsqlparser.nodes.oracle.TListaggOverflow;
import gudusoft.gsqlparser.nodes.oracle.TParallelEnableClause;
import gudusoft.gsqlparser.nodes.oracle.TPhysicalAttributesClause;
import gudusoft.gsqlparser.nodes.oracle.TPhysicalAttributesItem;
import gudusoft.gsqlparser.nodes.oracle.TPhysicalProperties;
import gudusoft.gsqlparser.nodes.oracle.TResultCacheClause;
import gudusoft.gsqlparser.nodes.oracle.TSegmentAttributesClause;
import gudusoft.gsqlparser.nodes.oracle.TSegmentAttributesItem;
import gudusoft.gsqlparser.nodes.oracle.TStorageClause;
import gudusoft.gsqlparser.nodes.oracle.TStorageItem;
import gudusoft.gsqlparser.nodes.oracle.TStreamingClause;
import gudusoft.gsqlparser.nodes.oracle.TTimingPoint;
import gudusoft.gsqlparser.nodes.teradata.TCollectColumnIndex;
import gudusoft.gsqlparser.nodes.teradata.TCollectFromOption;
import gudusoft.gsqlparser.nodes.teradata.TExpandOnClause;
import gudusoft.gsqlparser.nodes.teradata.TIndexDefinition;
import gudusoft.gsqlparser.nodes.teradata.TTDUnpivot;
import gudusoft.gsqlparser.nodes.teradata.TTeradataLockClause;
import gudusoft.gsqlparser.nodes.vertica.TTimeSeries;
import gudusoft.gsqlparser.stmt.TAlterAuditPolicyStmt;
import gudusoft.gsqlparser.stmt.TAlterDatabaseStmt;
import gudusoft.gsqlparser.stmt.TAlterFunctionStmt;
import gudusoft.gsqlparser.stmt.TAlterIndexStmt;
import gudusoft.gsqlparser.stmt.TAlterLibraryStmt;
import gudusoft.gsqlparser.stmt.TAlterMaterializedViewStmt;
import gudusoft.gsqlparser.stmt.TAlterRoleStmt;
import gudusoft.gsqlparser.stmt.TAlterSchemaStmt;
import gudusoft.gsqlparser.stmt.TAlterSequenceStatement;
import gudusoft.gsqlparser.stmt.TAlterSessionStatement;
import gudusoft.gsqlparser.stmt.TAlterSynonymStmt;
import gudusoft.gsqlparser.stmt.TAlterTableStatement;
import gudusoft.gsqlparser.stmt.TAlterTriggerStmt;
import gudusoft.gsqlparser.stmt.TAlterUserStmt;
import gudusoft.gsqlparser.stmt.TAlterViewStatement;
import gudusoft.gsqlparser.stmt.TAssignStmt;
import gudusoft.gsqlparser.stmt.TBeginTran;
import gudusoft.gsqlparser.stmt.TCallStatement;
import gudusoft.gsqlparser.stmt.TCaseStmt;
import gudusoft.gsqlparser.stmt.TCloseStmt;
import gudusoft.gsqlparser.stmt.TCommentOnSqlStmt;
import gudusoft.gsqlparser.stmt.TCommitStmt;
import gudusoft.gsqlparser.stmt.TCommonBlock;
import gudusoft.gsqlparser.stmt.TConnectStmt;
import gudusoft.gsqlparser.stmt.TCopyStmt;
import gudusoft.gsqlparser.stmt.TCreateAliasStmt;
import gudusoft.gsqlparser.stmt.TCreateAuditPolicyStmt;
import gudusoft.gsqlparser.stmt.TCreateDatabaseLinkStmt;
import gudusoft.gsqlparser.stmt.TCreateDatabaseSqlStatement;
import gudusoft.gsqlparser.stmt.TCreateFunctionStmt;
import gudusoft.gsqlparser.stmt.TCreateIndexSqlStatement;
import gudusoft.gsqlparser.stmt.TCreateMaterializedSqlStatement;
import gudusoft.gsqlparser.stmt.TCreateMaterializedViewLogSqlStatement;
import gudusoft.gsqlparser.stmt.TCreateProcedureStmt;
import gudusoft.gsqlparser.stmt.TCreateRoleStmt;
import gudusoft.gsqlparser.stmt.TCreateSchemaSqlStatement;
import gudusoft.gsqlparser.stmt.TCreateSequenceStmt;
import gudusoft.gsqlparser.stmt.TCreateSynonymStmt;
import gudusoft.gsqlparser.stmt.TCreateTableSqlStatement;
import gudusoft.gsqlparser.stmt.TCreateTablespaceStmt;
import gudusoft.gsqlparser.stmt.TCreateTriggerStmt;
import gudusoft.gsqlparser.stmt.TCreateUserStmt;
import gudusoft.gsqlparser.stmt.TCreateViewSqlStatement;
import gudusoft.gsqlparser.stmt.TCursorDeclStmt;
import gudusoft.gsqlparser.stmt.TDeleteSqlStatement;
import gudusoft.gsqlparser.stmt.TDescribeStmt;
import gudusoft.gsqlparser.stmt.TDisconnectStmt;
import gudusoft.gsqlparser.stmt.TDropDatabaseLinkStmt;
import gudusoft.gsqlparser.stmt.TDropDatabaseStmt;
import gudusoft.gsqlparser.stmt.TDropFunctionStmt;
import gudusoft.gsqlparser.stmt.TDropIndexSqlStatement;
import gudusoft.gsqlparser.stmt.TDropLibraryStmt;
import gudusoft.gsqlparser.stmt.TDropMaterializedViewLogStmt;
import gudusoft.gsqlparser.stmt.TDropMaterializedViewStmt;
import gudusoft.gsqlparser.stmt.TDropProcedureStmt;
import gudusoft.gsqlparser.stmt.TDropProfileStmt;
import gudusoft.gsqlparser.stmt.TDropProjectionStmt;
import gudusoft.gsqlparser.stmt.TDropRoleStmt;
import gudusoft.gsqlparser.stmt.TDropSchemaSqlStatement;
import gudusoft.gsqlparser.stmt.TDropSequenceStmt;
import gudusoft.gsqlparser.stmt.TDropStmt;
import gudusoft.gsqlparser.stmt.TDropSynonymStmt;
import gudusoft.gsqlparser.stmt.TDropTableSqlStatement;
import gudusoft.gsqlparser.stmt.TDropTriggerSqlStatement;
import gudusoft.gsqlparser.stmt.TDropUserStmt;
import gudusoft.gsqlparser.stmt.TDropViewSqlStatement;
import gudusoft.gsqlparser.stmt.TElsifStmt;
import gudusoft.gsqlparser.stmt.TEndTran;
import gudusoft.gsqlparser.stmt.TExecutePreparedStatement;
import gudusoft.gsqlparser.stmt.TExecuteSqlStatement;
import gudusoft.gsqlparser.stmt.TExitStmt;
import gudusoft.gsqlparser.stmt.TExplainPlan;
import gudusoft.gsqlparser.stmt.TFetchStmt;
import gudusoft.gsqlparser.stmt.TForStmt;
import gudusoft.gsqlparser.stmt.TGetDiagnosticsStmt;
import gudusoft.gsqlparser.stmt.TGrantStmt;
import gudusoft.gsqlparser.stmt.TIfStmt;
import gudusoft.gsqlparser.stmt.TInsertSqlStatement;
import gudusoft.gsqlparser.stmt.TLockTableStmt;
import gudusoft.gsqlparser.stmt.TLoopStmt;
import gudusoft.gsqlparser.stmt.TMSCKStmt;
import gudusoft.gsqlparser.stmt.TMergeSqlStatement;
import gudusoft.gsqlparser.stmt.TMssqlCreateType;
import gudusoft.gsqlparser.stmt.TOpenStmt;
import gudusoft.gsqlparser.stmt.TOpenforStmt;
import gudusoft.gsqlparser.stmt.TParseErrorSqlStatement;
import gudusoft.gsqlparser.stmt.TPrepareStmt;
import gudusoft.gsqlparser.stmt.TRaiseStmt;
import gudusoft.gsqlparser.stmt.TReindexStmt;
import gudusoft.gsqlparser.stmt.TReleaseSavepointStmt;
import gudusoft.gsqlparser.stmt.TRenameStmt;
import gudusoft.gsqlparser.stmt.TRepeatStmt;
import gudusoft.gsqlparser.stmt.TReturnStmt;
import gudusoft.gsqlparser.stmt.TRevokeStmt;
import gudusoft.gsqlparser.stmt.TRollbackStmt;
import gudusoft.gsqlparser.stmt.TSavepointStmt;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import gudusoft.gsqlparser.stmt.TSetDatabaseObjectStmt;
import gudusoft.gsqlparser.stmt.TSetStmt;
import gudusoft.gsqlparser.stmt.TShowStmt;
import gudusoft.gsqlparser.stmt.TStartTransactionStmt;
import gudusoft.gsqlparser.stmt.TTruncateStatement;
import gudusoft.gsqlparser.stmt.TUnknownSqlStatement;
import gudusoft.gsqlparser.stmt.TUnloadStmt;
import gudusoft.gsqlparser.stmt.TUnsetStmt;
import gudusoft.gsqlparser.stmt.TUpdateSqlStatement;
import gudusoft.gsqlparser.stmt.TUseDatabase;
import gudusoft.gsqlparser.stmt.TUseStmt;
import gudusoft.gsqlparser.stmt.TVacuumStmt;
import gudusoft.gsqlparser.stmt.TVarDeclStmt;
import gudusoft.gsqlparser.stmt.TWhileStmt;
import gudusoft.gsqlparser.stmt.couchbase.TInferKeyspaceStmt;
import gudusoft.gsqlparser.stmt.couchbase.TTBuildIndexesStmt;
import gudusoft.gsqlparser.stmt.dax.TDaxEvaluateStmt;
import gudusoft.gsqlparser.stmt.dax.TDaxExprStmt;
import gudusoft.gsqlparser.stmt.db2.TCreateVariableStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2CallStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2CaseStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2CloseCursorStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2ConditionDeclaration;
import gudusoft.gsqlparser.stmt.db2.TDb2CreateFunction;
import gudusoft.gsqlparser.stmt.db2.TDb2CreateProcedure;
import gudusoft.gsqlparser.stmt.db2.TDb2CreateTrigger;
import gudusoft.gsqlparser.stmt.db2.TDb2DeclareCursorStatement;
import gudusoft.gsqlparser.stmt.db2.TDb2DummyStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2FetchCursorStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2GotoStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2HandlerDeclaration;
import gudusoft.gsqlparser.stmt.db2.TDb2IfStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2IterateStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2LeaveStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2LoopStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2OpenCursorStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2ReturnCodesDeclaration;
import gudusoft.gsqlparser.stmt.db2.TDb2ReturnStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2ScriptOptionStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2SetStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2SetVariableStmt;
import gudusoft.gsqlparser.stmt.db2.TDb2SqlVariableDeclaration;
import gudusoft.gsqlparser.stmt.db2.TDb2StatementDeclaration;
import gudusoft.gsqlparser.stmt.db2.TDb2StmtStub;
import gudusoft.gsqlparser.stmt.db2.TRunStats;
import gudusoft.gsqlparser.stmt.greenplum.TSlashCommand;
import gudusoft.gsqlparser.stmt.hana.TAlterCredentialStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterFulltextIndexStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterJWTProviderStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterLDAPProviderStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterPSEStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterRemoteSourceStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterSAMLProviderStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterStatisticsStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterSystemStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterUserGroupStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterVirtualTableStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterWorkloadClassStmt;
import gudusoft.gsqlparser.stmt.hana.TAlterWorkloadMappingStmt;
import gudusoft.gsqlparser.stmt.hana.TBackupCancelStmt;
import gudusoft.gsqlparser.stmt.hana.TBackupCatalogDeleteStmt;
import gudusoft.gsqlparser.stmt.hana.TBackupCheckStmt;
import gudusoft.gsqlparser.stmt.hana.TBackupDataStmt;
import gudusoft.gsqlparser.stmt.hana.TBackupListDataStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateCertificateStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateCollectionStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateFulltextIndexStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateGraphWorkspaceStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateJWTProviderStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateLDAPProviderStmt;
import gudusoft.gsqlparser.stmt.hana.TCreatePSEStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateRemoteSourceStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateSAMLProviderStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateStatistics;
import gudusoft.gsqlparser.stmt.hana.TCreateStructuredPrivilegeStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateTypeStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateUserGroupStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateVirtualFunctionStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateVirtualProcedureStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateVirtualTableStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateWorkloadClassStmt;
import gudusoft.gsqlparser.stmt.hana.TCreateWorkloadMappingStmt;
import gudusoft.gsqlparser.stmt.hana.TExportStmt;
import gudusoft.gsqlparser.stmt.hana.TImportStmt;
import gudusoft.gsqlparser.stmt.hana.TLoadStmt;
import gudusoft.gsqlparser.stmt.hana.TMergeDeltaStmt;
import gudusoft.gsqlparser.stmt.hana.TRecoverDataStmt;
import gudusoft.gsqlparser.stmt.hana.TRecoverDatabaseStmt;
import gudusoft.gsqlparser.stmt.hana.TRefreshStatisticsStmt;
import gudusoft.gsqlparser.stmt.hana.TSignalStmt;
import gudusoft.gsqlparser.stmt.hana.TTruncateCollectionStmt;
import gudusoft.gsqlparser.stmt.hana.TValidateLDAPProviderStmt;
import gudusoft.gsqlparser.stmt.hana.TValidateUserStmt;
import gudusoft.gsqlparser.stmt.hive.THiveAnalyzeTable;
import gudusoft.gsqlparser.stmt.hive.THiveCreateFunction;
import gudusoft.gsqlparser.stmt.hive.THiveCreateRole;
import gudusoft.gsqlparser.stmt.hive.THiveDescribe;
import gudusoft.gsqlparser.stmt.hive.THiveDropDatabase;
import gudusoft.gsqlparser.stmt.hive.THiveDropFunction;
import gudusoft.gsqlparser.stmt.hive.THiveDropRole;
import gudusoft.gsqlparser.stmt.hive.THiveExplain;
import gudusoft.gsqlparser.stmt.hive.THiveExportTable;
import gudusoft.gsqlparser.stmt.hive.THiveFromQuery;
import gudusoft.gsqlparser.stmt.hive.THiveGrant;
import gudusoft.gsqlparser.stmt.hive.THiveGrantRole;
import gudusoft.gsqlparser.stmt.hive.THiveImportTable;
import gudusoft.gsqlparser.stmt.hive.THiveLoad;
import gudusoft.gsqlparser.stmt.hive.THiveRevoke;
import gudusoft.gsqlparser.stmt.hive.THiveRevokeRole;
import gudusoft.gsqlparser.stmt.hive.THiveSet;
import gudusoft.gsqlparser.stmt.hive.THiveShow;
import gudusoft.gsqlparser.stmt.hive.THiveShowGrant;
import gudusoft.gsqlparser.stmt.hive.THiveShowRoleGrant;
import gudusoft.gsqlparser.stmt.hive.THiveSwitchDatabase;
import gudusoft.gsqlparser.stmt.hive.THiveUnlockTable;
import gudusoft.gsqlparser.stmt.impala.TComputeStats;
import gudusoft.gsqlparser.stmt.informix.TInformixAllocateCollectionStmt;
import gudusoft.gsqlparser.stmt.informix.TInformixAllocateDescriptorStmt;
import gudusoft.gsqlparser.stmt.informix.TInformixAllocateRow;
import gudusoft.gsqlparser.stmt.informix.TInformixAlterAccess_MethodStmt;
import gudusoft.gsqlparser.stmt.informix.TInformixAlterFragment;
import gudusoft.gsqlparser.stmt.informix.TInformixCreateFunction;
import gudusoft.gsqlparser.stmt.informix.TInformixCreateProcedure;
import gudusoft.gsqlparser.stmt.informix.TInformixCreateRowTypeStmt;
import gudusoft.gsqlparser.stmt.informix.TInformixCreateTrigger;
import gudusoft.gsqlparser.stmt.informix.TInformixDropRowTypeStmt;
import gudusoft.gsqlparser.stmt.informix.TInformixExecuteFunction;
import gudusoft.gsqlparser.stmt.informix.TInformixExecuteImmediate;
import gudusoft.gsqlparser.stmt.informix.TInformixExecuteProcedure;
import gudusoft.gsqlparser.stmt.informix.TInformixExecuteStmt;
import gudusoft.gsqlparser.stmt.mdx.TMdxCall;
import gudusoft.gsqlparser.stmt.mdx.TMdxCreateMeasure;
import gudusoft.gsqlparser.stmt.mdx.TMdxCreateMember;
import gudusoft.gsqlparser.stmt.mdx.TMdxCreateSessionCube;
import gudusoft.gsqlparser.stmt.mdx.TMdxCreateSubCube;
import gudusoft.gsqlparser.stmt.mdx.TMdxDrillthrough;
import gudusoft.gsqlparser.stmt.mdx.TMdxExpression;
import gudusoft.gsqlparser.stmt.mdx.TMdxScope;
import gudusoft.gsqlparser.stmt.mdx.TMdxSelect;
import gudusoft.gsqlparser.stmt.mssql.TCreateEventSession;
import gudusoft.gsqlparser.stmt.mssql.TCreateExternalDataSourceStmt;
import gudusoft.gsqlparser.stmt.mssql.TCreateExternalLanguage;
import gudusoft.gsqlparser.stmt.mssql.TDenyStmt;
import gudusoft.gsqlparser.stmt.mssql.TMssqlBeginConversationTimer;
import gudusoft.gsqlparser.stmt.mssql.TMssqlBeginDialog;
import gudusoft.gsqlparser.stmt.mssql.TMssqlBlock;
import gudusoft.gsqlparser.stmt.mssql.TMssqlBulkInsert;
import gudusoft.gsqlparser.stmt.mssql.TMssqlClose;
import gudusoft.gsqlparser.stmt.mssql.TMssqlCommit;
import gudusoft.gsqlparser.stmt.mssql.TMssqlContinue;
import gudusoft.gsqlparser.stmt.mssql.TMssqlCreateFunction;
import gudusoft.gsqlparser.stmt.mssql.TMssqlCreateProcedure;
import gudusoft.gsqlparser.stmt.mssql.TMssqlCreateTrigger;
import gudusoft.gsqlparser.stmt.mssql.TMssqlCreateXmlSchemaCollectionStmt;
import gudusoft.gsqlparser.stmt.mssql.TMssqlDeallocate;
import gudusoft.gsqlparser.stmt.mssql.TMssqlDeclare;
import gudusoft.gsqlparser.stmt.mssql.TMssqlDropDbObject;
import gudusoft.gsqlparser.stmt.mssql.TMssqlDummyStmt;
import gudusoft.gsqlparser.stmt.mssql.TMssqlEndConversation;
import gudusoft.gsqlparser.stmt.mssql.TMssqlExecute;
import gudusoft.gsqlparser.stmt.mssql.TMssqlExecuteAs;
import gudusoft.gsqlparser.stmt.mssql.TMssqlFetch;
import gudusoft.gsqlparser.stmt.mssql.TMssqlGo;
import gudusoft.gsqlparser.stmt.mssql.TMssqlGoTo;
import gudusoft.gsqlparser.stmt.mssql.TMssqlGrant;
import gudusoft.gsqlparser.stmt.mssql.TMssqlIfElse;
import gudusoft.gsqlparser.stmt.mssql.TMssqlLabel;
import gudusoft.gsqlparser.stmt.mssql.TMssqlOpen;
import gudusoft.gsqlparser.stmt.mssql.TMssqlPrint;
import gudusoft.gsqlparser.stmt.mssql.TMssqlRaiserror;
import gudusoft.gsqlparser.stmt.mssql.TMssqlReturn;
import gudusoft.gsqlparser.stmt.mssql.TMssqlRevert;
import gudusoft.gsqlparser.stmt.mssql.TMssqlRollback;
import gudusoft.gsqlparser.stmt.mssql.TMssqlSaveTran;
import gudusoft.gsqlparser.stmt.mssql.TMssqlSendOnConversation;
import gudusoft.gsqlparser.stmt.mssql.TMssqlSet;
import gudusoft.gsqlparser.stmt.mssql.TMssqlSetRowCount;
import gudusoft.gsqlparser.stmt.mssql.TMssqlStmtStub;
import gudusoft.gsqlparser.stmt.mssql.TMssqlThrow;
import gudusoft.gsqlparser.stmt.mssql.TMssqlUpdateText;
import gudusoft.gsqlparser.stmt.mssql.TMssqlWaitFor;
import gudusoft.gsqlparser.stmt.mssql.TReconfigure;
import gudusoft.gsqlparser.stmt.mysql.TLoadDataStmt;
import gudusoft.gsqlparser.stmt.mysql.TMySQLBlock;
import gudusoft.gsqlparser.stmt.mysql.TMySQLCallStmt;
import gudusoft.gsqlparser.stmt.mysql.TMySQLCaseStmt;
import gudusoft.gsqlparser.stmt.mysql.TMySQLCreateFunction;
import gudusoft.gsqlparser.stmt.mysql.TMySQLCreateProcedure;
import gudusoft.gsqlparser.stmt.mysql.TMySQLCreateTrigger;
import gudusoft.gsqlparser.stmt.mysql.TMySQLDeallocatePrepareStmt;
import gudusoft.gsqlparser.stmt.mysql.TMySQLDeclare;
import gudusoft.gsqlparser.stmt.mysql.TMySQLFetchCursor;
import gudusoft.gsqlparser.stmt.mysql.TMySQLIfStmt;
import gudusoft.gsqlparser.stmt.mysql.TMySQLLoopStmt;
import gudusoft.gsqlparser.stmt.mysql.TMySQLOpenCursor;
import gudusoft.gsqlparser.stmt.mysql.TMySQLRepeatStmt;
import gudusoft.gsqlparser.stmt.mysql.TMySQLReturn;
import gudusoft.gsqlparser.stmt.mysql.TMySQLSet;
import gudusoft.gsqlparser.stmt.mysql.TMySQLShowStmt;
import gudusoft.gsqlparser.stmt.mysql.TMySQLSignal;
import gudusoft.gsqlparser.stmt.mysql.TMySQLSource;
import gudusoft.gsqlparser.stmt.mysql.TMySQLStmtStub;
import gudusoft.gsqlparser.stmt.mysql.TMySQLWhileStmt;
import gudusoft.gsqlparser.stmt.mysql.TShowIndexStmt;
import gudusoft.gsqlparser.stmt.netezza.TNetezzaGenerateStatistics;
import gudusoft.gsqlparser.stmt.netezza.TNetezzaGroomTable;
import gudusoft.gsqlparser.stmt.oracle.TBasicStmt;
import gudusoft.gsqlparser.stmt.oracle.TCompoundTriggerBody;
import gudusoft.gsqlparser.stmt.oracle.TOracleCreateDirectoryStmt;
import gudusoft.gsqlparser.stmt.oracle.TOracleCreateLibraryStmt;
import gudusoft.gsqlparser.stmt.oracle.TOracleExecuteProcedure;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlContinue;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlCreateFunction;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlCreatePackage;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlCreateProcedure;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlCreateTrigger;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlCreateType;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlCreateTypeBody;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlCreateType_Placeholder;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlDummyStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlExecImmeStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlForallStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlGotoStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlNullStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlPipeRowStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlPragmaDeclStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlProcedureSpecStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlRecordTypeDefStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlSqlStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlSubProgram;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlTableTypeDefStmt;
import gudusoft.gsqlparser.stmt.oracle.TPlsqlVarrayTypeDefStmt;
import gudusoft.gsqlparser.stmt.oracle.TSqlplusCmdStatement;
import gudusoft.gsqlparser.stmt.postgresql.TDropRoleSqlStatement;
import gudusoft.gsqlparser.stmt.postgresql.TForEachStmt;
import gudusoft.gsqlparser.stmt.postgresql.TMoveStmt;
import gudusoft.gsqlparser.stmt.postgresql.TNullStmt;
import gudusoft.gsqlparser.stmt.postgresql.TPerformanceStmt;
import gudusoft.gsqlparser.stmt.postgresql.TPgImport;
import gudusoft.gsqlparser.stmt.postgresql.TPlsqlCreateTriggerSqlStatement;
import gudusoft.gsqlparser.stmt.postgresql.TShowSearchPathStmt;
import gudusoft.gsqlparser.stmt.presto.TResetSessionStmt;
import gudusoft.gsqlparser.stmt.redshift.TFetchFromStmt;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftAbort;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftAlterGroup;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftAlterSchema;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftAlterUser;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftAnalyze;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftAnalyzeCompression;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftBegin;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftCancel;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftClose;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftComment;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftCommit;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftCopy;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftCreateGroup;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftCreateSchema;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftCreateUser;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftDeallocate;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftDeclare;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftDropGroup;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftDropSchema;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftDropUser;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftEnd;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftExplain;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftLock;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftPrepare;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftReset;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftRollback;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftSessionAuthorization;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftUnload;
import gudusoft.gsqlparser.stmt.redshift.TRedshiftVacuum;
import gudusoft.gsqlparser.stmt.snowflake.TAlterAccountStmt;
import gudusoft.gsqlparser.stmt.snowflake.TAlterFileFormatStmt;
import gudusoft.gsqlparser.stmt.snowflake.TAlterNetworkPolicyStmt;
import gudusoft.gsqlparser.stmt.snowflake.TAlterPipeStmt;
import gudusoft.gsqlparser.stmt.snowflake.TAlterResourceMonitorStmt;
import gudusoft.gsqlparser.stmt.snowflake.TAlterShareStmt;
import gudusoft.gsqlparser.stmt.snowflake.TAlterStageStmt;
import gudusoft.gsqlparser.stmt.snowflake.TAlterWarehouseStmt;
import gudusoft.gsqlparser.stmt.snowflake.TCreateFileFormatStmt;
import gudusoft.gsqlparser.stmt.snowflake.TCreateNetworkPolicyStmt;
import gudusoft.gsqlparser.stmt.snowflake.TCreatePipeStmt;
import gudusoft.gsqlparser.stmt.snowflake.TCreateResourceMonitorStmt;
import gudusoft.gsqlparser.stmt.snowflake.TCreateShareStmt;
import gudusoft.gsqlparser.stmt.snowflake.TCreateStageStmt;
import gudusoft.gsqlparser.stmt.snowflake.TCreateStreamStmt;
import gudusoft.gsqlparser.stmt.snowflake.TCreateWarehouseStmt;
import gudusoft.gsqlparser.stmt.snowflake.TGetStmt;
import gudusoft.gsqlparser.stmt.snowflake.TListStmt;
import gudusoft.gsqlparser.stmt.snowflake.TPseudoExprStmt;
import gudusoft.gsqlparser.stmt.snowflake.TPutStmt;
import gudusoft.gsqlparser.stmt.snowflake.TRemoveStmt;
import gudusoft.gsqlparser.stmt.snowflake.TUndropStmt;
import gudusoft.gsqlparser.stmt.snowflake.TUseRole;
import gudusoft.gsqlparser.stmt.snowflake.TUseSchema;
import gudusoft.gsqlparser.stmt.snowflake.TUseWarehouse;
import gudusoft.gsqlparser.stmt.sparksql.TCacheTable;
import gudusoft.gsqlparser.stmt.sparksql.TRefresh;
import gudusoft.gsqlparser.stmt.sparksql.TResourceManagement;
import gudusoft.gsqlparser.stmt.sybase.TInsertBulk;
import gudusoft.gsqlparser.stmt.sybase.TSybaseDeleteStatistics;
import gudusoft.gsqlparser.stmt.sybase.TSybaseDumpTran;
import gudusoft.gsqlparser.stmt.sybase.TSybaseUpdateIndexStatistics;
import gudusoft.gsqlparser.stmt.sybase.TSybaseWritetext;
import gudusoft.gsqlparser.stmt.teradata.TCheckWorkload;
import gudusoft.gsqlparser.stmt.teradata.TDropMacro;
import gudusoft.gsqlparser.stmt.teradata.TTeradataAbort;
import gudusoft.gsqlparser.stmt.teradata.TTeradataBTEQCmd;
import gudusoft.gsqlparser.stmt.teradata.TTeradataBeginLogging;
import gudusoft.gsqlparser.stmt.teradata.TTeradataBeginTransaction;
import gudusoft.gsqlparser.stmt.teradata.TTeradataCollectStatistics;
import gudusoft.gsqlparser.stmt.teradata.TTeradataCommit;
import gudusoft.gsqlparser.stmt.teradata.TTeradataCreateFunction;
import gudusoft.gsqlparser.stmt.teradata.TTeradataCreateMacro;
import gudusoft.gsqlparser.stmt.teradata.TTeradataCreateTrigger;
import gudusoft.gsqlparser.stmt.teradata.TTeradataDropDbObject;
import gudusoft.gsqlparser.stmt.teradata.TTeradataEndLogging;
import gudusoft.gsqlparser.stmt.teradata.TTeradataExecute;
import gudusoft.gsqlparser.stmt.teradata.TTeradataGive;
import gudusoft.gsqlparser.stmt.teradata.TTeradataGrant;
import gudusoft.gsqlparser.stmt.teradata.TTeradataLock;
import gudusoft.gsqlparser.stmt.teradata.TTeradataNotImplement;
import gudusoft.gsqlparser.stmt.teradata.TTeradataRollback;
import gudusoft.gsqlparser.stmt.teradata.TTeradataSetRole;
import gudusoft.gsqlparser.stmt.teradata.TTeradataSetSession;
import gudusoft.gsqlparser.stmt.teradata.TTeradataSetTimezone;
import gudusoft.gsqlparser.stmt.teradata.TTeradataStmtStub;
import gudusoft.gsqlparser.stmt.teradata.TTeradataUsing;
import gudusoft.gsqlparser.stmt.vertica.TAlterAccessPolicy;
import gudusoft.gsqlparser.stmt.vertica.TAlterAuthentication;
import gudusoft.gsqlparser.stmt.vertica.TAlterFaultGroup;
import gudusoft.gsqlparser.stmt.vertica.TAlterNetworkInterface;
import gudusoft.gsqlparser.stmt.vertica.TAlterNode;
import gudusoft.gsqlparser.stmt.vertica.TAlterProfile;
import gudusoft.gsqlparser.stmt.vertica.TAlterProjectionRename;
import gudusoft.gsqlparser.stmt.vertica.TAlterResourcePool;
import gudusoft.gsqlparser.stmt.vertica.TAlterSubnet;
import gudusoft.gsqlparser.stmt.vertica.TCreateAccessPolicy;
import gudusoft.gsqlparser.stmt.vertica.TCreateAuthentication;
import gudusoft.gsqlparser.stmt.vertica.TCreateFaultGroup;
import gudusoft.gsqlparser.stmt.vertica.TCreateHCatalogSchema;
import gudusoft.gsqlparser.stmt.vertica.TCreateSubnet;
import gudusoft.gsqlparser.stmt.vertica.TDropAccessPolicy;
import gudusoft.gsqlparser.stmt.vertica.TDropAggregateFunction;
import gudusoft.gsqlparser.stmt.vertica.TDropAuthentication;
import gudusoft.gsqlparser.stmt.vertica.TDropFaultGroup;
import gudusoft.gsqlparser.stmt.vertica.TDropNetworkInterface;
import gudusoft.gsqlparser.stmt.vertica.TDropResourcePool;
import gudusoft.gsqlparser.stmt.vertica.TDropSubnet;
import gudusoft.gsqlparser.stmt.vertica.TDropTextIndex;
import gudusoft.gsqlparser.stmt.vertica.TDropTransformFunction;
import gudusoft.gsqlparser.stmt.vertica.TExportToVertica;
import gudusoft.gsqlparser.stmt.vertica.TProfileStmt;
import gudusoft.gsqlparser.stmt.vertica.TVerticaCreateFunction;
import gudusoft.gsqlparser.stmt.vertica.TVerticaSetStmt;
import gudusoft.gsqlparser.stmt.vertica.TVerticaShow;

import java.util.Arrays;

public class TreeDump extends TParseTreeVisitor {

//    private static ImmutableMap<Integer, EExpressionType> exprTypeMap() {
//        ImmutableMap.Builder<Integer, EExpressionType> b = new ImmutableMap.Builder<>();
//        for(EExpressionType et : EExpressionType.values()) {
//
//        }
//            return null;
//    }

    private int indent = 0;
    private StringBuffer buf = new StringBuffer();

    public String getTreeString() {
        return buf.toString();
    }

    private void addNode(TParseTreeNode node) {
        for (int i = 0; i < indent; i++) {
            buf.append(' ');
        }

        String type = null;
        ENodeType eType = ENodeType.fromId(node.getNodeType());

        if (eType != ENodeType.T_Expression) {
            if (eType != null) {
                type = eType.name();
                type = type.startsWith("gudusoft.gsqlparser.nodes.") ? type.substring(26) : type;
            } else {
                type = "" + node.getNodeType();
            }

        } else {
            TExpression expr = (TExpression) node;
            type = "" + expr.getExpressionType();
        }
        buf.append("type=" + type);

        if (eType == ENodeType.T_Expression) {
            TExpression expr = (TExpression) node;
        }

        if (eType != ENodeType.T_Expression) {
            if (node.getStartToken() != null) {
                buf.append(",start=" + node.getStartToken().toString());
            }
            if (node.getEndToken() != null) {
                buf.append(", end=" + node.getEndToken().toString());
            }
        } else {
            buf.append(",expr=" + node.toString().replace("\n", " "));
        }
        buf.append("\n");
        indent += 1;
        // node.acceptChildren(this);
    }

    private void endNode(TParseTreeNode node) {
        indent -= 1;
    }


    public TreeDump() {
    }

    public void preVisit(TResetSessionStmt node) {
        addNode(node);
    }

    public void postVisit(TResetSessionStmt node) {
        endNode(node);
    }

    public void preVisit(TComputeStats node) {
        addNode(node);
    }

    public void postVisit(TComputeStats node) {
        endNode(node);
    }

    public void preVisit(TRefresh node) {
        addNode(node);
    }

    public void postVisit(TRefresh node) {
        endNode(node);
    }

    public void preVisit(TCacheTable node) {
        addNode(node);
    }

    public void postVisit(TCacheTable node) {
        endNode(node);
    }

    public void preVisit(TResourceManagement node) {
        addNode(node);
    }

    public void postVisit(TResourceManagement node) {
        endNode(node);
    }

    public void preVisit(TClusterBy node) {
        addNode(node);
    }

    public void postVisit(TClusterBy node) {
        endNode(node);
    }

    public void preVisit(TBlockSqlNode node) {
        addNode(node);
    }

    public void postVisit(TBlockSqlNode node) {
        endNode(node);
    }

    public void preVisit(TUseSchema node) {
        addNode(node);
    }

    public void postVisit(TUseSchema node) {
        endNode(node);
    }

    public void preVisit(TUseWarehouse node) {
        addNode(node);
    }

    public void postVisit(TUseWarehouse node) {
        endNode(node);
    }

    public void preVisit(TUseRole node) {
        addNode(node);
    }

    public void postVisit(TUseRole node) {
        endNode(node);
    }

    public void preVisit(TCreateStreamStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateStreamStmt node) {
        endNode(node);
    }

    public void preVisit(TListaggOverflow node) {
        addNode(node);
    }

    public void postVisit(TListaggOverflow node) {
        endNode(node);
    }

//    public void preVisit(TSequenceOption node) {
//        addNode(node);
//    }
//
//    public void postVisit(TSequenceOption node) {
//        endNode(node);
//    }
//
//    public void preVisit(TIdentityClause node) {
//        addNode(node);
//    }
//
//    public void postVisit(TIdentityClause node) {
//        endNode(node);
//    }

    public void preVisit(TCopyStmt node) {
        addNode(node);
    }

    public void postVisit(TCopyStmt node) {
        endNode(node);
    }

    public void preVisit(TParseTreeNodeList node) {
        addNode(node);
    }

    public void postVisit(TParseTreeNodeList node) {
        endNode(node);
    }

    public void preVisit(TDropIndexItemList node) {
        addNode(node);
    }

    public void postVisit(TDropIndexItemList node) {
        endNode(node);
    }

    public void preVisit(TDropIndexItem node) {
        addNode(node);
    }

    public void postVisit(TDropIndexItem node) {
        endNode(node);
    }

    public void preVisit(TPartitionClause node) {
        addNode(node);
    }

    public void postVisit(TPartitionClause node) {
        endNode(node);
    }

    public void preVisit(TPerformanceStmt node) {
        addNode(node);
    }

    public void postVisit(TPerformanceStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateExternalLanguage node) {
        addNode(node);
    }

    public void postVisit(TCreateExternalLanguage node) {
        endNode(node);
    }

    public void preVisit(TShowSearchPathStmt node) {
        addNode(node);
    }

    public void postVisit(TShowSearchPathStmt node) {
        endNode(node);
    }

    public void preVisit(TMySQLShowStmt node) {
        addNode(node);
    }

    public void postVisit(TMySQLShowStmt node) {
        endNode(node);
    }

    public void preVisit(TLoadDataStmt node) {
        addNode(node);
    }

    public void postVisit(TLoadDataStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateEventSession node) {
        addNode(node);
    }

    public void postVisit(TCreateEventSession node) {
        endNode(node);
    }

    public void preVisit(TOpenQuery node) {
        addNode(node);
    }

    public void postVisit(TOpenQuery node) {
        endNode(node);
    }

    public void preVisit(TRunStats node) {
        addNode(node);
    }

    public void postVisit(TRunStats node) {
        endNode(node);
    }

    public void preVisit(TCreateTablespaceStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateTablespaceStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateExternalDataSourceStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateExternalDataSourceStmt node) {
        endNode(node);
    }

    public void preVisit(TPseudoExprStmt node) {
        addNode(node);
    }

    public void postVisit(TPseudoExprStmt node) {
        endNode(node);
    }

    public void preVisit(TGetDiagnosticsStmt node) {
        addNode(node);
    }

    public void postVisit(TGetDiagnosticsStmt node) {
        endNode(node);
    }

    public void preVisit(TReindexStmt node) {
        addNode(node);
    }

    public void postVisit(TReindexStmt node) {
        endNode(node);
    }

    public void preVisit(TVacuumStmt node) {
        addNode(node);
    }

    public void postVisit(TVacuumStmt node) {
        endNode(node);
    }

    public void preVisit(TPgImport node) {
        addNode(node);
    }

    public void postVisit(TPgImport node) {
        endNode(node);
    }

    public void preVisit(TSelectModifier node) {
        addNode(node);
    }

    public void postVisit(TSelectModifier node) {
        endNode(node);
    }

    public void preVisit(TCheckWorkload node) {
        addNode(node);
    }

    public void postVisit(TCheckWorkload node) {
        endNode(node);
    }

    public void preVisit(TUnnestClause node) {
        addNode(node);
    }

    public void postVisit(TUnnestClause node) {
        endNode(node);
    }

    public void preVisit(TMySQLCreateTableOption node) {
        addNode(node);
    }

    public void postVisit(TMySQLCreateTableOption node) {
        endNode(node);
    }

    public void preVisit(TMssqlBeginConversationTimer node) {
        addNode(node);
    }

    public void postVisit(TMssqlBeginConversationTimer node) {
        endNode(node);
    }

    public void preVisit(TSegmentAttributesItem node) {
        addNode(node);
    }

    public void postVisit(TSegmentAttributesItem node) {
        endNode(node);
    }

    public void preVisit(TPhysicalProperties node) {
        addNode(node);
    }

    public void postVisit(TPhysicalProperties node) {
        endNode(node);
    }

    public void preVisit(TSegmentAttributesClause node) {
        addNode(node);
    }

    public void postVisit(TSegmentAttributesClause node) {
        endNode(node);
    }

    public void preVisit(TPhysicalAttributesItem node) {
        addNode(node);
    }

    public void postVisit(TPhysicalAttributesItem node) {
        endNode(node);
    }

    public void preVisit(TPhysicalAttributesClause node) {
        addNode(node);
    }

    public void postVisit(TPhysicalAttributesClause node) {
        endNode(node);
    }

    public void preVisit(TStorageClause node) {
        addNode(node);
    }

    public void postVisit(TStorageClause node) {
        endNode(node);
    }

    public void preVisit(TStorageItem node) {
        addNode(node);
    }

    public void postVisit(TStorageItem node) {
        endNode(node);
    }

    public void preVisit(TCreateMaterializedViewLogSqlStatement node) {
        addNode(node);
    }

    public void postVisit(TCreateMaterializedViewLogSqlStatement node) {
        endNode(node);
    }

    public void preVisit(TTDUnpivot node) {
        addNode(node);
    }

    public void postVisit(TTDUnpivot node) {
        endNode(node);
    }

    public void preVisit(TShowIndexStmt node) {
        addNode(node);
    }

    public void postVisit(TShowIndexStmt node) {
        endNode(node);
    }

    public void preVisit(TDenyStmt node) {
        addNode(node);
    }

    public void postVisit(TDenyStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterMaterializedViewStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterMaterializedViewStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateVariableStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateVariableStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateAliasStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateAliasStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2ScriptOptionStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2ScriptOptionStmt node) {
        endNode(node);
    }

    public void preVisit(TSignalStmt node) {
        addNode(node);
    }

    public void postVisit(TSignalStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateTriggerStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateTriggerStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateFunctionStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateFunctionStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateProcedureStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateProcedureStmt node) {
        endNode(node);
    }

    public void preVisit(TValidateLDAPProviderStmt node) {
        addNode(node);
    }

    public void postVisit(TValidateLDAPProviderStmt node) {
        endNode(node);
    }

    public void preVisit(TValidateUserStmt node) {
        addNode(node);
    }

    public void postVisit(TValidateUserStmt node) {
        endNode(node);
    }

    public void preVisit(TUnloadStmt node) {
        addNode(node);
    }

    public void postVisit(TUnloadStmt node) {
        endNode(node);
    }

    public void preVisit(TTruncateCollectionStmt node) {
        addNode(node);
    }

    public void postVisit(TTruncateCollectionStmt node) {
        endNode(node);
    }

    public void preVisit(TSetDatabaseObjectStmt node) {
        addNode(node);
    }

    public void postVisit(TSetDatabaseObjectStmt node) {
        endNode(node);
    }

    public void preVisit(TRefreshStatisticsStmt node) {
        addNode(node);
    }

    public void postVisit(TRefreshStatisticsStmt node) {
        endNode(node);
    }

    public void preVisit(TRecoverDatabaseStmt node) {
        addNode(node);
    }

    public void postVisit(TRecoverDatabaseStmt node) {
        endNode(node);
    }

    public void preVisit(TRecoverDataStmt node) {
        addNode(node);
    }

    public void postVisit(TRecoverDataStmt node) {
        endNode(node);
    }

    public void preVisit(TMergeDeltaStmt node) {
        addNode(node);
    }

    public void postVisit(TMergeDeltaStmt node) {
        endNode(node);
    }

    public void preVisit(TLoadStmt node) {
        addNode(node);
    }

    public void postVisit(TLoadStmt node) {
        endNode(node);
    }

    public void preVisit(TImportStmt node) {
        addNode(node);
    }

    public void postVisit(TImportStmt node) {
        endNode(node);
    }

    public void preVisit(TExportStmt node) {
        addNode(node);
    }

    public void postVisit(TExportStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateTypeStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateTypeStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateStatistics node) {
        addNode(node);
    }

    public void postVisit(TCreateStatistics node) {
        endNode(node);
    }

    public void preVisit(TCreateFulltextIndexStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateFulltextIndexStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateWorkloadMappingStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateWorkloadMappingStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateWorkloadClassStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateWorkloadClassStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateVirtualTableStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateVirtualTableStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateVirtualProcedureStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateVirtualProcedureStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateVirtualFunctionStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateVirtualFunctionStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateUserGroupStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateUserGroupStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateStructuredPrivilegeStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateStructuredPrivilegeStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateSAMLProviderStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateSAMLProviderStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateRemoteSourceStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateRemoteSourceStmt node) {
        endNode(node);
    }

    public void preVisit(TCreatePSEStmt node) {
        addNode(node);
    }

    public void postVisit(TCreatePSEStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateLDAPProviderStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateLDAPProviderStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateJWTProviderStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateJWTProviderStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateGraphWorkspaceStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateGraphWorkspaceStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateCollectionStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateCollectionStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateCertificateStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateCertificateStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateAuditPolicyStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateAuditPolicyStmt node) {
        endNode(node);
    }

    public void preVisit(TBackupListDataStmt node) {
        addNode(node);
    }

    public void postVisit(TBackupListDataStmt node) {
        endNode(node);
    }

    public void preVisit(TBackupDataStmt node) {
        addNode(node);
    }

    public void postVisit(TBackupDataStmt node) {
        endNode(node);
    }

    public void preVisit(TBackupCheckStmt node) {
        addNode(node);
    }

    public void postVisit(TBackupCheckStmt node) {
        endNode(node);
    }

    public void preVisit(TBackupCatalogDeleteStmt node) {
        addNode(node);
    }

    public void postVisit(TBackupCatalogDeleteStmt node) {
        endNode(node);
    }

    public void preVisit(TBackupCancelStmt node) {
        addNode(node);
    }

    public void postVisit(TBackupCancelStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterWorkloadMappingStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterWorkloadMappingStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterWorkloadClassStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterWorkloadClassStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterVirtualTableStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterVirtualTableStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterUserGroupStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterUserGroupStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterSystemStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterSystemStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterStatisticsStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterStatisticsStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterSAMLProviderStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterSAMLProviderStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterRemoteSourceStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterRemoteSourceStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterPSEStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterPSEStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterJWTProviderStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterJWTProviderStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterLDAPProviderStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterLDAPProviderStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterFulltextIndexStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterFulltextIndexStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterCredentialStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterCredentialStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterAuditPolicyStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterAuditPolicyStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterSynonymStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterSynonymStmt node) {
        endNode(node);
    }

    public void preVisit(TUseStmt node) {
        addNode(node);
    }

    public void postVisit(TUseStmt node) {
        endNode(node);
    }

    public void preVisit(TUnsetStmt node) {
        addNode(node);
    }

    public void postVisit(TUnsetStmt node) {
        endNode(node);
    }

    public void preVisit(TUndropStmt node) {
        addNode(node);
    }

    public void postVisit(TUndropStmt node) {
        endNode(node);
    }

    public void preVisit(TRemoveStmt node) {
        addNode(node);
    }

    public void postVisit(TRemoveStmt node) {
        endNode(node);
    }

    public void preVisit(TPutStmt node) {
        addNode(node);
    }

    public void postVisit(TPutStmt node) {
        endNode(node);
    }

    public void preVisit(TListStmt node) {
        addNode(node);
    }

    public void postVisit(TListStmt node) {
        endNode(node);
    }

    public void preVisit(TGetStmt node) {
        addNode(node);
    }

    public void postVisit(TGetStmt node) {
        endNode(node);
    }

    public void preVisit(TDropStmt node) {
        addNode(node);
    }

    public void postVisit(TDropStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateWarehouseStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateWarehouseStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateStageStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateStageStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateShareStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateShareStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateRoleStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateRoleStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateResourceMonitorStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateResourceMonitorStmt node) {
        endNode(node);
    }

    public void preVisit(TCreatePipeStmt node) {
        addNode(node);
    }

    public void postVisit(TCreatePipeStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateNetworkPolicyStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateNetworkPolicyStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateFileFormatStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateFileFormatStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterWarehouseStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterWarehouseStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterUserStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterUserStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterStageStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterStageStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterShareStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterShareStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterResourceMonitorStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterResourceMonitorStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterPipeStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterPipeStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterNetworkPolicyStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterNetworkPolicyStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterFileFormatStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterFileFormatStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterAccountStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterAccountStmt node) {
        endNode(node);
    }

    public void preVisit(TDropMaterializedViewStmt node) {
        addNode(node);
    }

    public void postVisit(TDropMaterializedViewStmt node) {
        endNode(node);
    }

    public void preVisit(TDropMaterializedViewLogStmt node) {
        addNode(node);
    }

    public void postVisit(TDropMaterializedViewLogStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateDatabaseLinkStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateDatabaseLinkStmt node) {
        endNode(node);
    }

    public void preVisit(TDropDatabaseLinkStmt node) {
        addNode(node);
    }

    public void postVisit(TDropDatabaseLinkStmt node) {
        endNode(node);
    }

    public void preVisit(TColumnWithSortOrder node) {
        addNode(node);
    }

    public void postVisit(TColumnWithSortOrder node) {
        endNode(node);
    }

    public void preVisit(TComputeExpr node) {
        addNode(node);
    }

    public void postVisit(TComputeExpr node) {
        endNode(node);
    }

    public void preVisit(TWithinGroup node) {
        addNode(node);
    }

    public void postVisit(TWithinGroup node) {
        endNode(node);
    }

    public void preVisit(TMySQLSource node) {
        addNode(node);
    }

    public void postVisit(TMySQLSource node) {
        endNode(node);
    }

    public void preVisit(TSlashCommand node) {
        addNode(node);
    }

    public void postVisit(TSlashCommand node) {
        endNode(node);
    }

    public void preVisit(TDropMacro node) {
        addNode(node);
    }

    public void postVisit(TDropMacro node) {
        endNode(node);
    }

    public void preVisit(TUpdateFor node) {
        addNode(node);
    }

    public void postVisit(TUpdateFor node) {
        endNode(node);
    }

    public void preVisit(TInferKeyspaceStmt node) {
        addNode(node);
    }

    public void postVisit(TInferKeyspaceStmt node) {
        endNode(node);
    }

    public void preVisit(TTBuildIndexesStmt node) {
        addNode(node);
    }

    public void postVisit(TTBuildIndexesStmt node) {
        endNode(node);
    }

    public void preVisit(TCollectionCondition node) {
        addNode(node);
    }

    public void postVisit(TCollectionCondition node) {
        endNode(node);
    }

    public void preVisit(TCollectionFirst node) {
        addNode(node);
    }

    public void postVisit(TCollectionFirst node) {
        endNode(node);
    }

    public void preVisit(TCollectionObject node) {
        addNode(node);
    }

    public void postVisit(TCollectionObject node) {
        endNode(node);
    }

    public void preVisit(TIndexRef node) {
        addNode(node);
    }

    public void postVisit(TIndexRef node) {
        endNode(node);
    }

    public void preVisit(TNamedParameter node) {
        addNode(node);
    }

    public void postVisit(TNamedParameter node) {
        endNode(node);
    }

    public void preVisit(TObjectConstruct node) {
        addNode(node);
    }

    public void postVisit(TObjectConstruct node) {
        endNode(node);
    }

    public void preVisit(TPositionalParameter node) {
        addNode(node);
    }

    public void postVisit(TPositionalParameter node) {
        endNode(node);
    }

    public void preVisit(TUseKeyIndex node) {
        addNode(node);
    }

    public void postVisit(TUseKeyIndex node) {
        endNode(node);
    }

    public void preVisit(TCollectionArray node) {
        addNode(node);
    }

    public void postVisit(TCollectionArray node) {
        endNode(node);
    }

    public void preVisit(TBinding node) {
        addNode(node);
    }

    public void postVisit(TBinding node) {
        endNode(node);
    }

    public void preVisit(TArrayConstruct node) {
        addNode(node);
    }

    public void postVisit(TArrayConstruct node) {
        endNode(node);
    }

    public void preVisit(TPair node) {
        addNode(node);
    }

    public void postVisit(TPair node) {
        endNode(node);
    }

    public void preVisit(TVerticaShow node) {
        addNode(node);
    }

    public void postVisit(TVerticaShow node) {
        endNode(node);
    }

    public void preVisit(TMySQLDeallocatePrepareStmt node) {
        addNode(node);
    }

    public void postVisit(TMySQLDeallocatePrepareStmt node) {
        endNode(node);
    }

    public void preVisit(TVerticaSetStmt node) {
        addNode(node);
    }

    public void postVisit(TVerticaSetStmt node) {
        endNode(node);
    }

    public void preVisit(TProfileStmt node) {
        addNode(node);
    }

    public void postVisit(TProfileStmt node) {
        endNode(node);
    }

    public void preVisit(TExportToVertica node) {
        addNode(node);
    }

    public void postVisit(TExportToVertica node) {
        endNode(node);
    }

    public void preVisit(TDropUserStmt node) {
        addNode(node);
    }

    public void postVisit(TDropUserStmt node) {
        endNode(node);
    }

    public void preVisit(TDropTransformFunction node) {
        addNode(node);
    }

    public void postVisit(TDropTransformFunction node) {
        endNode(node);
    }

    public void preVisit(TDropTextIndex node) {
        addNode(node);
    }

    public void postVisit(TDropTextIndex node) {
        endNode(node);
    }

    public void preVisit(TDropSubnet node) {
        addNode(node);
    }

    public void postVisit(TDropSubnet node) {
        endNode(node);
    }

    public void preVisit(TDropRoleStmt node) {
        addNode(node);
    }

    public void postVisit(TDropRoleStmt node) {
        endNode(node);
    }

    public void preVisit(TDropResourcePool node) {
        addNode(node);
    }

    public void postVisit(TDropResourcePool node) {
        endNode(node);
    }

    public void preVisit(TDropProjectionStmt node) {
        addNode(node);
    }

    public void postVisit(TDropProjectionStmt node) {
        endNode(node);
    }

    public void preVisit(TDropProfileStmt node) {
        addNode(node);
    }

    public void postVisit(TDropProfileStmt node) {
        endNode(node);
    }

    public void preVisit(TDropProcedureStmt node) {
        addNode(node);
    }

    public void postVisit(TDropProcedureStmt node) {
        endNode(node);
    }

    public void preVisit(TDropNetworkInterface node) {
        addNode(node);
    }

    public void postVisit(TDropNetworkInterface node) {
        endNode(node);
    }

    public void preVisit(TDropLibraryStmt node) {
        addNode(node);
    }

    public void postVisit(TDropLibraryStmt node) {
        endNode(node);
    }

    public void preVisit(TDropFunctionStmt node) {
        addNode(node);
    }

    public void postVisit(TDropFunctionStmt node) {
        endNode(node);
    }

    public void preVisit(TDropFaultGroup node) {
        addNode(node);
    }

    public void postVisit(TDropFaultGroup node) {
        endNode(node);
    }

    public void preVisit(TDropAuthentication node) {
        addNode(node);
    }

    public void postVisit(TDropAuthentication node) {
        endNode(node);
    }

    public void preVisit(TDropAggregateFunction node) {
        addNode(node);
    }

    public void postVisit(TDropAggregateFunction node) {
        endNode(node);
    }

    public void preVisit(TDropAccessPolicy node) {
        addNode(node);
    }

    public void postVisit(TDropAccessPolicy node) {
        endNode(node);
    }

    public void preVisit(TDisconnectStmt node) {
        addNode(node);
    }

    public void postVisit(TDisconnectStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateUserStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateUserStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateSubnet node) {
        addNode(node);
    }

    public void postVisit(TCreateSubnet node) {
        endNode(node);
    }

    public void preVisit(TCreateHCatalogSchema node) {
        addNode(node);
    }

    public void postVisit(TCreateHCatalogSchema node) {
        endNode(node);
    }

    public void preVisit(TVerticaCreateFunction node) {
        addNode(node);
    }

    public void postVisit(TVerticaCreateFunction node) {
        endNode(node);
    }

    public void preVisit(TCreateFaultGroup node) {
        addNode(node);
    }

    public void postVisit(TCreateFaultGroup node) {
        endNode(node);
    }

    public void preVisit(TCreateAuthentication node) {
        addNode(node);
    }

    public void postVisit(TCreateAuthentication node) {
        endNode(node);
    }

    public void preVisit(TCreateAccessPolicy node) {
        addNode(node);
    }

    public void postVisit(TCreateAccessPolicy node) {
        endNode(node);
    }

    public void preVisit(TConnectStmt node) {
        addNode(node);
    }

    public void postVisit(TConnectStmt node) {
        endNode(node);
    }

    public void preVisit(TReleaseSavepointStmt node) {
        addNode(node);
    }

    public void postVisit(TReleaseSavepointStmt node) {
        endNode(node);
    }

    public void preVisit(TSavepointStmt node) {
        addNode(node);
    }

    public void postVisit(TSavepointStmt node) {
        endNode(node);
    }

    public void preVisit(TRollbackStmt node) {
        addNode(node);
    }

    public void postVisit(TRollbackStmt node) {
        endNode(node);
    }

    public void preVisit(TCommitStmt node) {
        addNode(node);
    }

    public void postVisit(TCommitStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterSubnet node) {
        addNode(node);
    }

    public void postVisit(TAlterSubnet node) {
        endNode(node);
    }

    public void preVisit(TAlterSchemaStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterSchemaStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterRoleStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterRoleStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterResourcePool node) {
        addNode(node);
    }

    public void postVisit(TAlterResourcePool node) {
        endNode(node);
    }

    public void preVisit(TAlterProfile node) {
        addNode(node);
    }

    public void postVisit(TAlterProfile node) {
        endNode(node);
    }

    public void preVisit(TAlterProjectionRename node) {
        addNode(node);
    }

    public void postVisit(TAlterProjectionRename node) {
        endNode(node);
    }

    public void preVisit(TAlterNetworkInterface node) {
        addNode(node);
    }

    public void postVisit(TAlterNetworkInterface node) {
        endNode(node);
    }

    public void preVisit(TAlterNode node) {
        addNode(node);
    }

    public void postVisit(TAlterNode node) {
        endNode(node);
    }

    public void preVisit(TAlterLibraryStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterLibraryStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterFunctionStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterFunctionStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterFaultGroup node) {
        addNode(node);
    }

    public void postVisit(TAlterFaultGroup node) {
        endNode(node);
    }

    public void preVisit(TAlterAuthentication node) {
        addNode(node);
    }

    public void postVisit(TAlterAuthentication node) {
        endNode(node);
    }

    public void preVisit(TNameValuePair node) {
        addNode(node);
    }

    public void postVisit(TNameValuePair node) {
        endNode(node);
    }

    public void preVisit(TAlterAccessPolicy node) {
        addNode(node);
    }

    public void postVisit(TAlterAccessPolicy node) {
        endNode(node);
    }

    public void preVisit(TWithTableLock node) {
        addNode(node);
    }

    public void postVisit(TWithTableLock node) {
        endNode(node);
    }

    public void preVisit(TAlterSequenceStatement node) {
        addNode(node);
    }

    public void postVisit(TAlterSequenceStatement node) {
        endNode(node);
    }

    public void preVisit(TTimeSeries node) {
        addNode(node);
    }

    public void postVisit(TTimeSeries node) {
        endNode(node);
    }

    public void preVisit(TMdxCreateMeasure node) {
        addNode(node);
    }

    public void postVisit(TMdxCreateMeasure node) {
        endNode(node);
    }

    public void preVisit(TDaxSummarize node) {
        addNode(node);
    }

    public void postVisit(TDaxSummarize node) {
        endNode(node);
    }

    public void preVisit(TDaxReturn node) {
        addNode(node);
    }

    public void postVisit(TDaxReturn node) {
        endNode(node);
    }

    public void preVisit(TDaxVar node) {
        addNode(node);
    }

    public void postVisit(TDaxVar node) {
        endNode(node);
    }

    public void preVisit(TDaxSummerizeColumns node) {
        addNode(node);
    }

    public void postVisit(TDaxSummerizeColumns node) {
        endNode(node);
    }

    public void preVisit(TDaxGroupBy node) {
        addNode(node);
    }

    public void postVisit(TDaxGroupBy node) {
        endNode(node);
    }

    public void preVisit(TDaxDatatable node) {
        addNode(node);
    }

    public void postVisit(TDaxDatatable node) {
        endNode(node);
    }

    public void preVisit(TTeradataCreateMacro node) {
        addNode(node);
    }

    public void postVisit(TTeradataCreateMacro node) {
        endNode(node);
    }

    public void preVisit(TDaxIsOnOrAfter node) {
        addNode(node);
    }

    public void postVisit(TDaxIsOnOrAfter node) {
        endNode(node);
    }

    public void preVisit(TDaxExprPair node) {
        addNode(node);
    }

    public void postVisit(TDaxExprPair node) {
        endNode(node);
    }

    public void preVisit(TDaxSubstituteWithIndex node) {
        addNode(node);
    }

    public void postVisit(TDaxSubstituteWithIndex node) {
        endNode(node);
    }

    public void preVisit(TDaxAddMissingItems node) {
        addNode(node);
    }

    public void postVisit(TDaxAddMissingItems node) {
        endNode(node);
    }

    public void preVisit(TDaxFunction node) {
        addNode(node);
    }

    public void postVisit(TDaxFunction node) {
        endNode(node);
    }

    public void preVisit(TDaxEvaluateStmt node) {
        addNode(node);
    }

    public void postVisit(TDaxEvaluateStmt node) {
        endNode(node);
    }

    public void preVisit(TDaxExprStmt node) {
        addNode(node);
    }

    public void postVisit(TDaxExprStmt node) {
        endNode(node);
    }

    public void preVisit(THintClause node) {
        addNode(node);
    }

    public void postVisit(THintClause node) {
        endNode(node);
    }

    public void preVisit(TMssqlCreateXmlSchemaCollectionStmt node) {
        addNode(node);
    }

    public void postVisit(TMssqlCreateXmlSchemaCollectionStmt node) {
        endNode(node);
    }

    public void preVisit(TSelectDistinct node) {
        addNode(node);
    }

    public void postVisit(TSelectDistinct node) {
        endNode(node);
    }

    public void preVisit(TExpressionCallTarget node) {
        addNode(node);
    }

    public void postVisit(TExpressionCallTarget node) {
        endNode(node);
    }

    public void preVisit(TExecuteAsClause node) {
        addNode(node);
    }

    public void postVisit(TExecuteAsClause node) {
        endNode(node);
    }

    public void preVisit(TProcedureOption node) {
        addNode(node);
    }

    public void postVisit(TProcedureOption node) {
        endNode(node);
    }

    public void preVisit(TExecParameterList node) {
        addNode(node);
    }

    public void postVisit(TExecParameterList node) {
        endNode(node);
    }

    public void preVisit(TMssqlCreateType node) {
        addNode(node);
    }

    public void postVisit(TMssqlCreateType node) {
        endNode(node);
    }

    public void preVisit(TGroupConcatParam node) {
        addNode(node);
    }

    public void postVisit(TGroupConcatParam node) {
        endNode(node);
    }

    public void preVisit(TExpandOnClause node) {
        addNode(node);
    }

    public void postVisit(TExpandOnClause node) {
        endNode(node);
    }

    public void preVisit(TXMLPassingClause node) {
        addNode(node);
    }

    public void postVisit(TXMLPassingClause node) {
        endNode(node);
    }

    public void preVisit(TXMLAttributesClause node) {
        addNode(node);
    }

    public void postVisit(TXMLAttributesClause node) {
        endNode(node);
    }

    public void preVisit(TTimingPoint node) {
        addNode(node);
    }

    public void postVisit(TTimingPoint node) {
        endNode(node);
    }

    public void preVisit(TStreamingClause node) {
        addNode(node);
    }

    public void postVisit(TStreamingClause node) {
        endNode(node);
    }

    public void preVisit(TResultCacheClause node) {
        addNode(node);
    }

    public void postVisit(TResultCacheClause node) {
        endNode(node);
    }

    public void preVisit(TParallelEnableClause node) {
        addNode(node);
    }

    public void postVisit(TParallelEnableClause node) {
        endNode(node);
    }

    public void preVisit(TInvokerRightsClause node) {
        addNode(node);
    }

    public void postVisit(TInvokerRightsClause node) {
        endNode(node);
    }

    public void preVisit(TModeChoice node) {
        addNode(node);
    }

    public void postVisit(TModeChoice node) {
        endNode(node);
    }

    public void preVisit(TReclaimChoice node) {
        addNode(node);
    }

    public void postVisit(TReclaimChoice node) {
        endNode(node);
    }

    public void preVisit(TQueryHint node) {
        addNode(node);
    }

    public void postVisit(TQueryHint node) {
        endNode(node);
    }

    public void preVisit(TOptionClause node) {
        addNode(node);
    }

    public void postVisit(TOptionClause node) {
        endNode(node);
    }

    public void preVisit(TMssqlCreateTriggerUpdateColumn node) {
        addNode(node);
    }

    public void postVisit(TMssqlCreateTriggerUpdateColumn node) {
        endNode(node);
    }

    public void preVisit(THiveRecordReader node) {
        addNode(node);
    }

    public void postVisit(THiveRecordReader node) {
        endNode(node);
    }

    public void preVisit(THivePrivilegeDef node) {
        addNode(node);
    }

    public void postVisit(THivePrivilegeDef node) {
        endNode(node);
    }

    public void preVisit(THiveRecordWriter node) {
        addNode(node);
    }

    public void postVisit(THiveRecordWriter node) {
        endNode(node);
    }

    public void preVisit(THiveRowFormat node) {
        addNode(node);
    }

    public void postVisit(THiveRowFormat node) {
        endNode(node);
    }

    public void preVisit(THiveTableBuckets node) {
        addNode(node);
    }

    public void postVisit(THiveTableBuckets node) {
        endNode(node);
    }

    public void preVisit(THiveTableFileFormat node) {
        addNode(node);
    }

    public void postVisit(THiveTableFileFormat node) {
        endNode(node);
    }

    public void preVisit(THiveTablePartition node) {
        addNode(node);
    }

    public void postVisit(THiveTablePartition node) {
        endNode(node);
    }

    public void preVisit(THiveTableProperties node) {
        addNode(node);
    }

    public void postVisit(THiveTableProperties node) {
        endNode(node);
    }

    public void preVisit(THiveTableSkewed node) {
        addNode(node);
    }

    public void postVisit(THiveTableSkewed node) {
        endNode(node);
    }

    public void preVisit(THiveTerminatedIdentifier node) {
        addNode(node);
    }

    public void postVisit(THiveTerminatedIdentifier node) {
        endNode(node);
    }

    public void preVisit(THiveTransformClause node) {
        addNode(node);
    }

    public void postVisit(THiveTransformClause node) {
        endNode(node);
    }

    public void preVisit(THiveVariable node) {
        addNode(node);
    }

    public void postVisit(THiveVariable node) {
        endNode(node);
    }

    public void preVisit(THiveWithDBPropertiesClause node) {
        addNode(node);
    }

    public void postVisit(THiveWithDBPropertiesClause node) {
        endNode(node);
    }

    public void preVisit(THivePrincipalName node) {
        addNode(node);
    }

    public void postVisit(THivePrincipalName node) {
        endNode(node);
    }

    public void preVisit(THivePartitionedTableFunction node) {
        addNode(node);
    }

    public void postVisit(THivePartitionedTableFunction node) {
        endNode(node);
    }

    public void preVisit(TLateralView node) {
        addNode(node);
    }

    public void postVisit(TLateralView node) {
        endNode(node);
    }

    public void preVisit(THiveKeyValueProperty node) {
        addNode(node);
    }

    public void postVisit(THiveKeyValueProperty node) {
        endNode(node);
    }

    public void preVisit(THiveIndexProperties node) {
        addNode(node);
    }

    public void postVisit(THiveIndexProperties node) {
        endNode(node);
    }

    public void preVisit(THiveHintItem node) {
        addNode(node);
    }

    public void postVisit(THiveHintItem node) {
        endNode(node);
    }

    public void preVisit(THiveHintClause node) {
        addNode(node);
    }

    public void postVisit(THiveHintClause node) {
        endNode(node);
    }

    public void preVisit(THiveDescTablePartition node) {
        addNode(node);
    }

    public void postVisit(THiveDescTablePartition node) {
        endNode(node);
    }

    public void preVisit(TWindowSpecification node) {
        addNode(node);
    }

    public void postVisit(TWindowSpecification node) {
        endNode(node);
    }

    public void preVisit(TWindowPartitioningSpec node) {
        addNode(node);
    }

    public void postVisit(TWindowPartitioningSpec node) {
        endNode(node);
    }

    public void preVisit(TWindowFrameBoundary node) {
        addNode(node);
    }

    public void postVisit(TWindowFrameBoundary node) {
        endNode(node);
    }

    public void preVisit(TWindowFrame node) {
        addNode(node);
    }

    public void postVisit(TWindowFrame node) {
        endNode(node);
    }

    public void preVisit(TWindowDefinition node) {
        addNode(node);
    }

    public void postVisit(TWindowDefinition node) {
        endNode(node);
    }

    public void preVisit(TWindowDef node) {
        addNode(node);
    }

    public void postVisit(TWindowDef node) {
        endNode(node);
    }

    public void preVisit(TWindowClause node) {
        addNode(node);
    }

    public void postVisit(TWindowClause node) {
        endNode(node);
    }

    public void preVisit(TSubscripts node) {
        addNode(node);
    }

    public void postVisit(TSubscripts node) {
        endNode(node);
    }

    public void preVisit(TTriggeringClause node) {
        addNode(node);
    }

    public void postVisit(TTriggeringClause node) {
        endNode(node);
    }

    public void preVisit(TSimpleDmlTriggerClause node) {
        addNode(node);
    }

    public void postVisit(TSimpleDmlTriggerClause node) {
        endNode(node);
    }

    public void preVisit(TNonDmlTriggerClause node) {
        addNode(node);
    }

    public void postVisit(TNonDmlTriggerClause node) {
        endNode(node);
    }

    public void preVisit(TCompoundDmlTriggerClause node) {
        addNode(node);
    }

    public void postVisit(TCompoundDmlTriggerClause node) {
        endNode(node);
    }

    public void preVisit(TDmlEventItem node) {
        addNode(node);
    }

    public void postVisit(TDmlEventItem node) {
        endNode(node);
    }

    public void preVisit(TDdlEventItem node) {
        addNode(node);
    }

    public void postVisit(TDdlEventItem node) {
        endNode(node);
    }

    public void preVisit(TDatabaseEventItem node) {
        addNode(node);
    }

    public void postVisit(TDatabaseEventItem node) {
        endNode(node);
    }

    public void preVisit(TExecParameter node) {
        addNode(node);
    }

    public void postVisit(TExecParameter node) {
        endNode(node);
    }

    public void preVisit(TCreateSchemaSqlStatement node) {
        addNode(node);
    }

    public void postVisit(TCreateSchemaSqlStatement node) {
        endNode(node);
    }

    public void preVisit(TCompoundTriggerBody node) {
        addNode(node);
    }

    public void postVisit(TCompoundTriggerBody node) {
        endNode(node);
    }

    public void preVisit(TKeepDenseRankClause node) {
        addNode(node);
    }

    public void postVisit(TKeepDenseRankClause node) {
        endNode(node);
    }

    public void preVisit(TTableSample node) {
        addNode(node);
    }

    public void postVisit(TTableSample node) {
        endNode(node);
    }

    public void preVisit(TInsertCondition node) {
        addNode(node);
    }

    public void postVisit(TInsertCondition node) {
        endNode(node);
    }

    public void preVisit(TInsertIntoValue node) {
        addNode(node);
    }

    public void postVisit(TInsertIntoValue node) {
        endNode(node);
    }

    public void preVisit(TMergeDeleteClause node) {
        addNode(node);
    }

    public void postVisit(TMergeDeleteClause node) {
        endNode(node);
    }

    public void preVisit(TAnalyticFunction node) {
        addNode(node);
    }

    public void postVisit(TAnalyticFunction node) {
        endNode(node);
    }

    public void preVisit(TReconfigure node) {
        addNode(node);
    }

    public void postVisit(TReconfigure node) {
        endNode(node);
    }

    public void preVisit(TConnectByClause node) {
        addNode(node);
    }

    public void postVisit(TConnectByClause node) {
        endNode(node);
    }

    public void preVisit(TUnpivotInClauseItem node) {
        addNode(node);
    }

    public void postVisit(TUnpivotInClauseItem node) {
        endNode(node);
    }

    public void preVisit(TUnpivotInClause node) {
        addNode(node);
    }

    public void postVisit(TUnpivotInClause node) {
        endNode(node);
    }

    public void preVisit(TPivotInClause node) {
        addNode(node);
    }

    public void postVisit(TPivotInClause node) {
        endNode(node);
    }

    public void preVisit(TPivotedTable node) {
        addNode(node);
    }

    public void postVisit(TPivotedTable node) {
        endNode(node);
    }

    public void preVisit(TTableElement node) {
        addNode(node);
    }

    public void postVisit(TTableElement node) {
        endNode(node);
    }

    public void preVisit(TAlterTableOption node) {
        addNode(node);
    }

    public void postVisit(TAlterTableOption node) {
        endNode(node);
    }

    public void preVisit(TObjectName node) {
        addNode(node);
    }

    public void postVisit(TObjectName node) {
        endNode(node);
    }

    public void preVisit(TObjectNameList node) {
        addNode(node);
    }

    public void postVisit(TObjectNameList node) {
        endNode(node);
    }

    public void preVisit(TConstant node) {
        addNode(node);
    }

    public void postVisit(TConstant node) {
        endNode(node);
    }

    public void preVisit(TConstantList node) {
        addNode(node);
    }

    public void postVisit(TConstantList node) {
        endNode(node);
    }

    public void preVisit(TExpression node) {
        addNode(node);
    }

    public void postVisit(TExpression node) {
        endNode(node);
    }

    public void preVisit(TExpressionList node) {
        addNode(node);
    }

    public void postVisit(TExpressionList node) {
        endNode(node);
    }

    public void preVisit(TInExpr node) {
        addNode(node);
    }

    public void postVisit(TInExpr node) {
        endNode(node);
    }

    public void preVisit(TGroupingExpressionItem node) {
        addNode(node);
    }

    public void postVisit(TGroupingExpressionItem node) {
        endNode(node);
    }

    public void preVisit(TGroupingExpressionItemList node) {
        addNode(node);
    }

    public void postVisit(TGroupingExpressionItemList node) {
        endNode(node);
    }

    public void preVisit(TResultColumn node) {
        addNode(node);
        // preOrderTraverse(node.getExpr());
    }

    public void postVisit(TResultColumn node) {
        endNode(node);
    }

    public void preVisit(TResultColumnList node) {
        addNode(node);
    }

    public void postVisit(TResultColumnList node) {
        endNode(node);
    }

    public void preVisit(TTable node) {
        addNode(node);
    }

    public void postVisit(TTable node) {
        endNode(node);
    }

    public void preVisit(TCTE node) {
        addNode(node);
    }

    public void postVisit(TCTE node) {
        endNode(node);
    }

    public void preVisit(TCTEList node) {
        addNode(node);
    }

    public void postVisit(TCTEList node) {
        endNode(node);
    }

    public void preVisit(TTopClause node) {
        addNode(node);
    }

    public void postVisit(TTopClause node) {
        endNode(node);
    }

    public void preVisit(TIntoClause node) {
        addNode(node);
    }

    public void postVisit(TIntoClause node) {
        endNode(node);
    }

    public void preVisit(TPivotClause node) {
        addNode(node);
    }

    public void postVisit(TPivotClause node) {
        endNode(node);
    }

    public void preVisit(TCaseExpression node) {
        addNode(node);
    }

    public void postVisit(TCaseExpression node) {
        endNode(node);
    }

    public void preVisit(TWhenClauseItem node) {
        addNode(node);
    }

    public void postVisit(TWhenClauseItem node) {
        endNode(node);
    }

    public void preVisit(TWhenClauseItemList node) {
        addNode(node);
    }

    public void postVisit(TWhenClauseItemList node) {
        endNode(node);
    }

    public void preVisit(TJoin node) {
        addNode(node);
    }

    public void postVisit(TJoin node) {
        endNode(node);
    }

    public void preVisit(TJoinList node) {
        addNode(node);
    }

    public void postVisit(TJoinList node) {
        endNode(node);
    }

    public void preVisit(TJoinItem node) {
        addNode(node);
    }

    public void postVisit(TJoinItem node) {
        endNode(node);
    }

    public void preVisit(TJoinItemList node) {
        addNode(node);
    }

    public void postVisit(TJoinItemList node) {
        endNode(node);
    }

    public void preVisit(TWhereClause node) {
        addNode(node);
        // preOrderTraverse(node.getCondition());
    }

    public void postVisit(TWhereClause node) {
        endNode(node);
    }

    public void preVisit(TOpenDatasource node) {
        addNode(node);
    }

    public void postVisit(TOpenDatasource node) {
        endNode(node);
    }

    public void preVisit(TContainsTable node) {
        addNode(node);
    }

    public void postVisit(TContainsTable node) {
        endNode(node);
    }

    public void preVisit(TOpenXML node) {
        addNode(node);
    }

    public void postVisit(TOpenXML node) {
        endNode(node);
    }

    public void preVisit(TOpenRowSet node) {
        addNode(node);
    }

    public void postVisit(TOpenRowSet node) {
        endNode(node);
    }

    public void preVisit(TTypeAttribute node) {
        addNode(node);
    }

    public void postVisit(TTypeAttribute node) {
        endNode(node);
    }

    public void preVisit(TTypeAttributeList node) {
        addNode(node);
    }

    public void postVisit(TTypeAttributeList node) {
        endNode(node);
    }

    public void preVisit(TErrorLoggingClause node) {
        addNode(node);
    }

    public void postVisit(TErrorLoggingClause node) {
        endNode(node);
    }

    public void preVisit(TMergeWhenClause node) {
        addNode(node);
    }

    public void postVisit(TMergeWhenClause node) {
        endNode(node);
    }

    public void preVisit(TMergeUpdateClause node) {
        addNode(node);
    }

    public void postVisit(TMergeUpdateClause node) {
        endNode(node);
    }

    public void preVisit(TMergeInsertClause node) {
        addNode(node);
    }

    public void postVisit(TMergeInsertClause node) {
        endNode(node);
    }

    public void preVisit(TDb2CallStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2CallStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2CaseStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2CaseStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2CloseCursorStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2CloseCursorStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2ConditionDeclaration node) {
        addNode(node);
    }

    public void postVisit(TDb2ConditionDeclaration node) {
        endNode(node);
    }

    public void preVisit(TDb2CreateFunction node) {
        addNode(node);
    }

    public void postVisit(TDb2CreateFunction node) {
        endNode(node);
    }

    public void preVisit(TDb2CreateProcedure node) {
        addNode(node);
    }

    public void postVisit(TDb2CreateProcedure node) {
        endNode(node);
    }

    public void preVisit(TDb2CreateTrigger node) {
        addNode(node);
    }

    public void postVisit(TDb2CreateTrigger node) {
        endNode(node);
    }

    public void preVisit(TDb2DeclareCursorStatement node) {
        addNode(node);
    }

    public void postVisit(TDb2DeclareCursorStatement node) {
        endNode(node);
    }

    public void preVisit(TDb2DummyStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2DummyStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2FetchCursorStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2FetchCursorStmt node) {
        endNode(node);
    }

    public void preVisit(TForStmt node) {
        addNode(node);
    }

    public void postVisit(TForStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2GotoStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2GotoStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2HandlerDeclaration node) {
        addNode(node);
    }

    public void postVisit(TDb2HandlerDeclaration node) {
        endNode(node);
    }

    public void preVisit(TDb2IfStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2IfStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2IterateStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2IterateStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2LeaveStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2LeaveStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2LoopStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2LoopStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2OpenCursorStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2OpenCursorStmt node) {
        endNode(node);
    }

    public void preVisit(TRepeatStmt node) {
        addNode(node);
    }

    public void postVisit(TRepeatStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2ReturnCodesDeclaration node) {
        addNode(node);
    }

    public void postVisit(TDb2ReturnCodesDeclaration node) {
        endNode(node);
    }

    public void preVisit(TDb2ReturnStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2ReturnStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2SetStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2SetStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2SetVariableStmt node) {
        addNode(node);
    }

    public void postVisit(TDb2SetVariableStmt node) {
        endNode(node);
    }

    public void preVisit(TSetAssignment node) {
        addNode(node);
    }

    public void postVisit(TSetAssignment node) {
        endNode(node);
    }

    public void preVisit(gudusoft.gsqlparser.stmt.TSignalStmt node) {
        addNode(node);
    }

    public void postVisit(gudusoft.gsqlparser.stmt.TSignalStmt node) {
        endNode(node);
    }

    public void preVisit(TDb2SqlVariableDeclaration node) {
        addNode(node);
    }

    public void postVisit(TDb2SqlVariableDeclaration node) {
        endNode(node);
    }

    public void preVisit(TDb2StatementDeclaration node) {
        addNode(node);
    }

    public void postVisit(TDb2StatementDeclaration node) {
        endNode(node);
    }

    public void preVisit(TWhileStmt node) {
        addNode(node);
    }

    public void postVisit(TWhileStmt node) {
        endNode(node);
    }

    public void preVisit(TMySQLCaseStmt node) {
        addNode(node);
    }

    public void postVisit(TMySQLCaseStmt node) {
        endNode(node);
    }

    public void preVisit(TMySQLBlock node) {
        addNode(node);
    }

    public void postVisit(TMySQLBlock node) {
        endNode(node);
    }

    public void preVisit(TMySQLCallStmt node) {
        addNode(node);
    }

    public void postVisit(TMySQLCallStmt node) {
        endNode(node);
    }

    public void preVisit(TMySQLFetchCursor node) {
        addNode(node);
    }

    public void postVisit(TMySQLFetchCursor node) {
        endNode(node);
    }

    public void preVisit(TMySQLIfStmt node) {
        addNode(node);
    }

    public void postVisit(TMySQLIfStmt node) {
        endNode(node);
    }

    public void preVisit(TMySQLLoopStmt node) {
        addNode(node);
    }

    public void postVisit(TMySQLLoopStmt node) {
        endNode(node);
    }

    public void preVisit(TMySQLOpenCursor node) {
        addNode(node);
    }

    public void postVisit(TMySQLOpenCursor node) {
        endNode(node);
    }

    public void preVisit(TPrepareStmt node) {
        addNode(node);
    }

    public void postVisit(TPrepareStmt node) {
        endNode(node);
    }

    public void preVisit(TMySQLRepeatStmt node) {
        addNode(node);
    }

    public void postVisit(TMySQLRepeatStmt node) {
        endNode(node);
    }

    public void preVisit(TMySQLReturn node) {
        addNode(node);
    }

    public void postVisit(TMySQLReturn node) {
        endNode(node);
    }

    public void preVisit(TMySQLSet node) {
        addNode(node);
    }

    public void postVisit(TMySQLSet node) {
        endNode(node);
    }

    public void preVisit(TMySQLWhileStmt node) {
        addNode(node);
    }

    public void postVisit(TMySQLWhileStmt node) {
        endNode(node);
    }

    public void preVisit(TMySQLSignal node) {
        addNode(node);
    }

    public void postVisit(TMySQLSignal node) {
        endNode(node);
    }

    public void preVisit(TAlterDatabaseStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterDatabaseStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterIndexStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterIndexStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterTriggerStmt node) {
        addNode(node);
    }

    public void postVisit(TAlterTriggerStmt node) {
        endNode(node);
    }

    public void preVisit(TAlterViewStatement node) {
        addNode(node);
    }

    public void postVisit(TAlterViewStatement node) {
        endNode(node);
    }

    public void preVisit(TCreateDatabaseSqlStatement node) {
        addNode(node);
    }

    public void postVisit(TCreateDatabaseSqlStatement node) {
        endNode(node);
    }

    public void preVisit(TDescribeStmt node) {
        addNode(node);
    }

    public void postVisit(TDescribeStmt node) {
        endNode(node);
    }

    public void preVisit(TDropSchemaSqlStatement node) {
        addNode(node);
    }

    public void postVisit(TDropSchemaSqlStatement node) {
        endNode(node);
    }

    public void preVisit(TDropSequenceStmt node) {
        addNode(node);
    }

    public void postVisit(TDropSequenceStmt node) {
        endNode(node);
    }

    public void preVisit(TDropSynonymStmt node) {
        addNode(node);
    }

    public void postVisit(TDropSynonymStmt node) {
        endNode(node);
    }

    public void preVisit(TExplainPlan node) {
        addNode(node);
    }

    public void postVisit(TExplainPlan node) {
        endNode(node);
    }

    public void preVisit(TGrantStmt node) {
        addNode(node);
    }

    public void postVisit(TGrantStmt node) {
        endNode(node);
    }

    public void preVisit(TParseErrorSqlStatement node) {
        addNode(node);
    }

    public void postVisit(TParseErrorSqlStatement node) {
        endNode(node);
    }

    public void preVisit(TRenameStmt node) {
        addNode(node);
    }

    public void postVisit(TRenameStmt node) {
        endNode(node);
    }

    public void preVisit(TRevokeStmt node) {
        addNode(node);
    }

    public void postVisit(TRevokeStmt node) {
        endNode(node);
    }

    public void preVisit(TTeradataExecute node) {
        addNode(node);
    }

    public void postVisit(TTeradataExecute node) {
        endNode(node);
    }

    public void preVisit(TEndTran node) {
        addNode(node);
    }

    public void postVisit(TEndTran node) {
        endNode(node);
    }

    public void preVisit(TTeradataEndLogging node) {
        addNode(node);
    }

    public void postVisit(TTeradataEndLogging node) {
        endNode(node);
    }

    public void preVisit(TTeradataDropDbObject node) {
        addNode(node);
    }

    public void postVisit(TTeradataDropDbObject node) {
        endNode(node);
    }

    public void preVisit(TTeradataCreateTrigger node) {
        addNode(node);
    }

    public void postVisit(TTeradataCreateTrigger node) {
        endNode(node);
    }

    public void preVisit(TTeradataCreateFunction node) {
        addNode(node);
    }

    public void postVisit(TTeradataCreateFunction node) {
        endNode(node);
    }

    public void preVisit(TTeradataCommit node) {
        addNode(node);
    }

    public void postVisit(TTeradataCommit node) {
        endNode(node);
    }

    public void preVisit(TTeradataCollectStatistics node) {
        addNode(node);
    }

    public void postVisit(TTeradataCollectStatistics node) {
        endNode(node);
    }

    public void preVisit(TTeradataBTEQCmd node) {
        addNode(node);
    }

    public void postVisit(TTeradataBTEQCmd node) {
        endNode(node);
    }

    public void preVisit(TTeradataBeginTransaction node) {
        addNode(node);
    }

    public void postVisit(TTeradataBeginTransaction node) {
        endNode(node);
    }

    public void preVisit(TTeradataSetTimezone node) {
        addNode(node);
    }

    public void postVisit(TTeradataSetTimezone node) {
        endNode(node);
    }

    public void preVisit(TTeradataSetSession node) {
        addNode(node);
    }

    public void postVisit(TTeradataSetSession node) {
        endNode(node);
    }

    public void preVisit(TTeradataSetRole node) {
        addNode(node);
    }

    public void postVisit(TTeradataSetRole node) {
        endNode(node);
    }

    public void preVisit(TTeradataRollback node) {
        addNode(node);
    }

    public void postVisit(TTeradataRollback node) {
        endNode(node);
    }

    public void preVisit(TTeradataNotImplement node) {
        addNode(node);
    }

    public void postVisit(TTeradataNotImplement node) {
        endNode(node);
    }

    public void preVisit(TTeradataGrant node) {
        addNode(node);
    }

    public void postVisit(TTeradataGrant node) {
        endNode(node);
    }

    public void preVisit(TTeradataLock node) {
        addNode(node);
    }

    public void postVisit(TTeradataLock node) {
        endNode(node);
    }

    public void preVisit(TTeradataUsing node) {
        addNode(node);
    }

    public void postVisit(TTeradataUsing node) {
        endNode(node);
    }

    public void preVisit(TTeradataBeginLogging node) {
        addNode(node);
    }

    public void postVisit(TTeradataBeginLogging node) {
        endNode(node);
    }

    public void preVisit(TTeradataAbort node) {
        addNode(node);
    }

    public void postVisit(TTeradataAbort node) {
        endNode(node);
    }

    public void preVisit(TSybaseDumpTran node) {
        addNode(node);
    }

    public void postVisit(TSybaseDumpTran node) {
        endNode(node);
    }

    public void preVisit(TSybaseDeleteStatistics node) {
        addNode(node);
    }

    public void postVisit(TSybaseDeleteStatistics node) {
        endNode(node);
    }

    public void preVisit(TInsertBulk node) {
        addNode(node);
    }

    public void postVisit(TInsertBulk node) {
        endNode(node);
    }

    public void preVisit(TSybaseUpdateIndexStatistics node) {
        addNode(node);
    }

    public void postVisit(TSybaseUpdateIndexStatistics node) {
        endNode(node);
    }

    public void preVisit(TSybaseWritetext node) {
        addNode(node);
    }

    public void postVisit(TSybaseWritetext node) {
        endNode(node);
    }

    public void preVisit(TFetchFromStmt node) {
        addNode(node);
    }

    public void postVisit(TFetchFromStmt node) {
        endNode(node);
    }

    public void preVisit(TRedshiftAbort node) {
        addNode(node);
    }

    public void postVisit(TRedshiftAbort node) {
        endNode(node);
    }

    public void preVisit(TRedshiftAlterGroup node) {
        addNode(node);
    }

    public void postVisit(TRedshiftAlterGroup node) {
        endNode(node);
    }

    public void preVisit(TRedshiftAlterSchema node) {
        addNode(node);
    }

    public void postVisit(TRedshiftAlterSchema node) {
        endNode(node);
    }

    public void preVisit(TRedshiftAlterUser node) {
        addNode(node);
    }

    public void postVisit(TRedshiftAlterUser node) {
        endNode(node);
    }

    public void preVisit(TRedshiftAnalyze node) {
        addNode(node);
    }

    public void postVisit(TRedshiftAnalyze node) {
        endNode(node);
    }

    public void preVisit(TRedshiftAnalyzeCompression node) {
        addNode(node);
    }

    public void postVisit(TRedshiftAnalyzeCompression node) {
        endNode(node);
    }

    public void preVisit(TRedshiftBegin node) {
        addNode(node);
    }

    public void postVisit(TRedshiftBegin node) {
        endNode(node);
    }

    public void preVisit(TRedshiftCancel node) {
        addNode(node);
    }

    public void postVisit(TRedshiftCancel node) {
        endNode(node);
    }

    public void preVisit(TRedshiftClose node) {
        addNode(node);
    }

    public void postVisit(TRedshiftClose node) {
        endNode(node);
    }

    public void preVisit(TRedshiftComment node) {
        addNode(node);
    }

    public void postVisit(TRedshiftComment node) {
        endNode(node);
    }

    public void preVisit(TRedshiftCommit node) {
        addNode(node);
    }

    public void postVisit(TRedshiftCommit node) {
        endNode(node);
    }

    public void preVisit(TRedshiftCopy node) {
        addNode(node);
    }

    public void postVisit(TRedshiftCopy node) {
        endNode(node);
    }

    public void preVisit(TRedshiftCreateGroup node) {
        addNode(node);
    }

    public void postVisit(TRedshiftCreateGroup node) {
        endNode(node);
    }

    public void preVisit(TRedshiftCreateSchema node) {
        addNode(node);
    }

    public void postVisit(TRedshiftCreateSchema node) {
        endNode(node);
    }

    public void preVisit(TRedshiftCreateUser node) {
        addNode(node);
    }

    public void postVisit(TRedshiftCreateUser node) {
        endNode(node);
    }

    public void preVisit(TRedshiftDeallocate node) {
        addNode(node);
    }

    public void postVisit(TRedshiftDeallocate node) {
        endNode(node);
    }

    public void preVisit(TRedshiftDeclare node) {
        addNode(node);
    }

    public void postVisit(TRedshiftDeclare node) {
        endNode(node);
    }

    public void preVisit(TDropDatabaseStmt node) {
        addNode(node);
    }

    public void postVisit(TDropDatabaseStmt node) {
        endNode(node);
    }

    public void preVisit(TRedshiftDropGroup node) {
        addNode(node);
    }

    public void postVisit(TRedshiftDropGroup node) {
        endNode(node);
    }

    public void preVisit(TRedshiftDropSchema node) {
        addNode(node);
    }

    public void postVisit(TRedshiftDropSchema node) {
        endNode(node);
    }

    public void preVisit(TRedshiftDropUser node) {
        addNode(node);
    }

    public void postVisit(TRedshiftDropUser node) {
        endNode(node);
    }

    public void preVisit(TRedshiftEnd node) {
        addNode(node);
    }

    public void postVisit(TRedshiftEnd node) {
        endNode(node);
    }

    public void preVisit(TRedshiftExplain node) {
        addNode(node);
    }

    public void postVisit(TRedshiftExplain node) {
        endNode(node);
    }

    public void preVisit(TRedshiftLock node) {
        addNode(node);
    }

    public void postVisit(TRedshiftLock node) {
        endNode(node);
    }

    public void preVisit(TRedshiftReset node) {
        addNode(node);
    }

    public void postVisit(TRedshiftReset node) {
        endNode(node);
    }

    public void preVisit(TRedshiftRollback node) {
        addNode(node);
    }

    public void postVisit(TRedshiftRollback node) {
        endNode(node);
    }

    public void preVisit(TRedshiftSessionAuthorization node) {
        addNode(node);
    }

    public void postVisit(TRedshiftSessionAuthorization node) {
        endNode(node);
    }

    public void preVisit(TSetStmt node) {
        addNode(node);
    }

    public void postVisit(TSetStmt node) {
        endNode(node);
    }

    public void preVisit(TShowStmt node) {
        addNode(node);
    }

    public void postVisit(TShowStmt node) {
        endNode(node);
    }

    public void preVisit(TStartTransactionStmt node) {
        addNode(node);
    }

    public void postVisit(TStartTransactionStmt node) {
        endNode(node);
    }

    public void preVisit(TRedshiftUnload node) {
        addNode(node);
    }

    public void postVisit(TRedshiftUnload node) {
        endNode(node);
    }

    public void preVisit(TRedshiftVacuum node) {
        addNode(node);
    }

    public void postVisit(TRedshiftVacuum node) {
        endNode(node);
    }

    public void preVisit(TRedshiftPrepare node) {
        addNode(node);
    }

    public void postVisit(TRedshiftPrepare node) {
        endNode(node);
    }

    public void preVisit(TPlsqlCreateTriggerSqlStatement node) {
        addNode(node);
    }

    public void postVisit(TPlsqlCreateTriggerSqlStatement node) {
        endNode(node);
    }

    public void preVisit(TNullStmt node) {
        addNode(node);
    }

    public void postVisit(TNullStmt node) {
        endNode(node);
    }

    public void preVisit(TMoveStmt node) {
        addNode(node);
    }

    public void postVisit(TMoveStmt node) {
        endNode(node);
    }

    public void preVisit(TForEachStmt node) {
        addNode(node);
    }

    public void postVisit(TForEachStmt node) {
        endNode(node);
    }

    public void preVisit(TExecuteSqlStatement node) {
        addNode(node);
    }

    public void postVisit(TExecuteSqlStatement node) {
        endNode(node);
    }

    public void preVisit(TDropTriggerSqlStatement node) {
        addNode(node);
    }

    public void postVisit(TDropTriggerSqlStatement node) {
        endNode(node);
    }

    public void preVisit(TDropRoleSqlStatement node) {
        addNode(node);
    }

    public void postVisit(TDropRoleSqlStatement node) {
        endNode(node);
    }

    public void preVisit(TNetezzaGroomTable node) {
        addNode(node);
    }

    public void postVisit(TNetezzaGroomTable node) {
        endNode(node);
    }

    public void preVisit(TNetezzaGenerateStatistics node) {
        addNode(node);
    }

    public void postVisit(TNetezzaGenerateStatistics node) {
        endNode(node);
    }

    public void preVisit(TMssqlWaitFor node) {
        addNode(node);
    }

    public void postVisit(TMssqlWaitFor node) {
        endNode(node);
    }

    public void preVisit(TMssqlSetRowCount node) {
        addNode(node);
    }

    public void postVisit(TMssqlSetRowCount node) {
        endNode(node);
    }

    public void preVisit(TMssqlDropDbObject node) {
        addNode(node);
    }

    public void postVisit(TMssqlDropDbObject node) {
        endNode(node);
    }

    public void preVisit(TMssqlDummyStmt node) {
        addNode(node);
    }

    public void postVisit(TMssqlDummyStmt node) {
        endNode(node);
    }

    public void preVisit(TInformixAllocateCollectionStmt node) {
        addNode(node);
    }

    public void postVisit(TInformixAllocateCollectionStmt node) {
        endNode(node);
    }

    public void preVisit(TInformixAllocateDescriptorStmt node) {
        addNode(node);
    }

    public void postVisit(TInformixAllocateDescriptorStmt node) {
        endNode(node);
    }

    public void preVisit(TInformixAllocateRow node) {
        addNode(node);
    }

    public void postVisit(TInformixAllocateRow node) {
        endNode(node);
    }

    public void preVisit(TInformixAlterAccess_MethodStmt node) {
        addNode(node);
    }

    public void postVisit(TInformixAlterAccess_MethodStmt node) {
        endNode(node);
    }

    public void preVisit(TInformixAlterFragment node) {
        addNode(node);
    }

    public void postVisit(TInformixAlterFragment node) {
        endNode(node);
    }

    public void preVisit(TInformixCreateFunction node) {
        addNode(node);
    }

    public void postVisit(TInformixCreateFunction node) {
        endNode(node);
    }

    public void preVisit(TInformixCreateProcedure node) {
        addNode(node);
    }

    public void postVisit(TInformixCreateProcedure node) {
        endNode(node);
    }

    public void preVisit(TInformixCreateRowTypeStmt node) {
        addNode(node);
    }

    public void postVisit(TInformixCreateRowTypeStmt node) {
        endNode(node);
    }

    public void preVisit(TInformixCreateTrigger node) {
        addNode(node);
    }

    public void postVisit(TInformixCreateTrigger node) {
        endNode(node);
    }

    public void preVisit(TInformixDropRowTypeStmt node) {
        addNode(node);
    }

    public void postVisit(TInformixDropRowTypeStmt node) {
        endNode(node);
    }

    public void preVisit(TInformixExecuteFunction node) {
        addNode(node);
    }

    public void postVisit(TInformixExecuteFunction node) {
        endNode(node);
    }

    public void preVisit(TInformixExecuteImmediate node) {
        addNode(node);
    }

    public void postVisit(TInformixExecuteImmediate node) {
        endNode(node);
    }

    public void preVisit(TInformixExecuteProcedure node) {
        addNode(node);
    }

    public void postVisit(TInformixExecuteProcedure node) {
        endNode(node);
    }

    public void preVisit(TInformixExecuteStmt node) {
        addNode(node);
    }

    public void postVisit(TInformixExecuteStmt node) {
        endNode(node);
    }

    public void preVisit(THiveLoad node) {
        addNode(node);
    }

    public void postVisit(THiveLoad node) {
        endNode(node);
    }

    public void preVisit(THiveImportTable node) {
        addNode(node);
    }

    public void postVisit(THiveImportTable node) {
        endNode(node);
    }

    public void preVisit(THiveGrantRole node) {
        addNode(node);
    }

    public void postVisit(THiveGrantRole node) {
        endNode(node);
    }

    public void preVisit(THiveGrant node) {
        addNode(node);
    }

    public void postVisit(THiveGrant node) {
        endNode(node);
    }

    public void preVisit(THiveExportTable node) {
        addNode(node);
    }

    public void postVisit(THiveExportTable node) {
        endNode(node);
    }

    public void preVisit(THiveExplain node) {
        addNode(node);
    }

    public void postVisit(THiveExplain node) {
        endNode(node);
    }

    public void preVisit(THiveDropRole node) {
        addNode(node);
    }

    public void postVisit(THiveDropRole node) {
        endNode(node);
    }

    public void preVisit(THiveDropFunction node) {
        addNode(node);
    }

    public void postVisit(THiveDropFunction node) {
        endNode(node);
    }

    public void preVisit(THiveDropDatabase node) {
        addNode(node);
    }

    public void postVisit(THiveDropDatabase node) {
        endNode(node);
    }

    public void preVisit(THiveDescribe node) {
        addNode(node);
    }

    public void postVisit(THiveDescribe node) {
        endNode(node);
    }

    public void preVisit(THiveCreateRole node) {
        addNode(node);
    }

    public void postVisit(THiveCreateRole node) {
        endNode(node);
    }

    public void preVisit(THiveFromQuery node) {
        addNode(node);
    }

    public void postVisit(THiveFromQuery node) {
        endNode(node);
    }

    public void preVisit(THiveAnalyzeTable node) {
        addNode(node);
    }

    public void postVisit(THiveAnalyzeTable node) {
        endNode(node);
    }

    public void preVisit(THiveCreateFunction node) {
        addNode(node);
    }

    public void postVisit(THiveCreateFunction node) {
        endNode(node);
    }

    public void preVisit(THiveUnlockTable node) {
        addNode(node);
    }

    public void postVisit(THiveUnlockTable node) {
        endNode(node);
    }

    public void preVisit(THiveSwitchDatabase node) {
        addNode(node);
    }

    public void postVisit(THiveSwitchDatabase node) {
        endNode(node);
    }

    public void preVisit(THiveShowRoleGrant node) {
        addNode(node);
    }

    public void postVisit(THiveShowRoleGrant node) {
        endNode(node);
    }

    public void preVisit(THiveShowGrant node) {
        addNode(node);
    }

    public void postVisit(THiveShowGrant node) {
        endNode(node);
    }

    public void preVisit(THiveShow node) {
        addNode(node);
    }

    public void postVisit(THiveShow node) {
        endNode(node);
    }

    public void preVisit(THiveSet node) {
        addNode(node);
    }

    public void postVisit(THiveSet node) {
        endNode(node);
    }

    public void preVisit(THiveRevokeRole node) {
        addNode(node);
    }

    public void postVisit(THiveRevokeRole node) {
        endNode(node);
    }

    public void preVisit(THiveRevoke node) {
        addNode(node);
    }

    public void postVisit(THiveRevoke node) {
        endNode(node);
    }

    public void preVisit(TMSCKStmt node) {
        addNode(node);
    }

    public void postVisit(TMSCKStmt node) {
        endNode(node);
    }

    public void preVisit(TLockTableStmt node) {
        addNode(node);
    }

    public void postVisit(TLockTableStmt node) {
        endNode(node);
    }

    public void preVisit(THierarchical node) {
        addNode(node);
    }

    public void postVisit(THierarchical node) {
        endNode(node);
    }

    public void preVisit(TGroupBy node) {
        addNode(node);
    }

    public void postVisit(TGroupBy node) {
        endNode(node);
    }

    public void preVisit(TGroupByItem node) {
        addNode(node);
    }

    public void postVisit(TGroupByItem node) {
        endNode(node);
    }

    public void preVisit(TGroupByItemList node) {
        addNode(node);
    }

    public void postVisit(TGroupByItemList node) {
        endNode(node);
    }

    public void preVisit(TOrderBy node) {
        addNode(node);
    }

    public void postVisit(TOrderBy node) {
        endNode(node);
    }

    public void preVisit(TOrderByItem node) {
        addNode(node);
    }

    public void postVisit(TOrderByItem node) {
        endNode(node);
    }

    public void preVisit(TOrderByItemList node) {
        addNode(node);
    }

    public void postVisit(TOrderByItemList node) {
        endNode(node);
    }

    public void preVisit(TForUpdate node) {
        addNode(node);
    }

    public void postVisit(TForUpdate node) {
        endNode(node);
    }

    public void preVisit(TComputeClause node) {
        addNode(node);
    }

    public void postVisit(TComputeClause node) {
        endNode(node);
    }

    public void preVisit(TComputeClauseItem node) {
        addNode(node);
    }

    public void postVisit(TComputeClauseItem node) {
        endNode(node);
    }

    public void preVisit(TComputeClauseItemList node) {
        addNode(node);
    }

    public void postVisit(TComputeClauseItemList node) {
        endNode(node);
    }

    public void preVisit(TAliasClause node) {
        addNode(node);
    }

    public void postVisit(TAliasClause node) {
        endNode(node);
    }

    public void preVisit(TStatementList node) {
        addNode(node);
    }

    public void postVisit(TStatementList node) {
        endNode(node);
    }

    public void preVisit(TCommonBlock node) {
        addNode(node);
    }

    public void postVisit(TCommonBlock node) {
        endNode(node);
    }

    public void preVisit(TExceptionClause node) {
        addNode(node);
    }

    public void postVisit(TExceptionClause node) {
        endNode(node);
    }

    public void preVisit(TExceptionHandler node) {
        addNode(node);
    }

    public void postVisit(TExceptionHandler node) {
        endNode(node);
    }

    public void preVisit(TExceptionHandlerList node) {
        addNode(node);
    }

    public void postVisit(TExceptionHandlerList node) {
        endNode(node);
    }

    public void preVisit(TAssignStmt node) {
        addNode(node);
    }

    public void postVisit(TAssignStmt node) {
        endNode(node);
    }

    public void preVisit(TBasicStmt node) {
        addNode(node);
    }

    public void postVisit(TBasicStmt node) {
        endNode(node);
    }

    public void preVisit(TCaseStmt node) {
        addNode(node);
    }

    public void postVisit(TCaseStmt node) {
        endNode(node);
    }

    public void preVisit(TCloseStmt node) {
        addNode(node);
    }

    public void postVisit(TCloseStmt node) {
        endNode(node);
    }

    public void preVisit(TPlsqlCreateFunction node) {
        addNode(node);
    }

    public void postVisit(TPlsqlCreateFunction node) {
        endNode(node);
    }

    public void preVisit(TPlsqlCreatePackage node) {
        addNode(node);
    }

    public void postVisit(TPlsqlCreatePackage node) {
        endNode(node);
    }

    public void preVisit(TPlsqlCreateProcedure node) {
        addNode(node);
    }

    public void postVisit(TPlsqlCreateProcedure node) {
        endNode(node);
    }

    public void preVisit(TPlsqlCreateTrigger node) {
        addNode(node);
    }

    public void postVisit(TPlsqlCreateTrigger node) {
        endNode(node);
    }

    public void preVisit(TPlsqlCreateType node) {
        addNode(node);
    }

    public void postVisit(TPlsqlCreateType node) {
        endNode(node);
    }

    public void preVisit(TPlsqlCreateType_Placeholder node) {
        addNode(node);
    }

    public void postVisit(TPlsqlCreateType_Placeholder node) {
        endNode(node);
    }

    public void preVisit(TPlsqlCreateTypeBody node) {
        addNode(node);
    }

    public void postVisit(TPlsqlCreateTypeBody node) {
        endNode(node);
    }

    public void preVisit(TCallStatement node) {
        addNode(node);
    }

    public void postVisit(TCallStatement node) {
        endNode(node);
    }

    public void preVisit(TOracleCreateDirectoryStmt node) {
        addNode(node);
    }

    public void postVisit(TOracleCreateDirectoryStmt node) {
        endNode(node);
    }

    public void preVisit(TPlsqlContinue node) {
        addNode(node);
    }

    public void postVisit(TPlsqlContinue node) {
        endNode(node);
    }

    public void preVisit(TOracleCreateLibraryStmt node) {
        addNode(node);
    }

    public void postVisit(TOracleCreateLibraryStmt node) {
        endNode(node);
    }

    public void postVisit(TCursorDeclStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlDummyStmt node) {
        endNode(node);
    }

    public void postVisit(TElsifStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlExecImmeStmt node) {
        endNode(node);
    }

    public void postVisit(TExitStmt node) {
        endNode(node);
    }

    public void postVisit(TFetchStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlForallStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlGotoStmt node) {
        endNode(node);
    }

    public void postVisit(TIfStmt node) {
        endNode(node);
    }

    public void postVisit(TLoopStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlNullStmt node) {
        endNode(node);
    }

    public void postVisit(TOpenforStmt node) {
        endNode(node);
    }

    public void postVisit(TOpenStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlPipeRowStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlPragmaDeclStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlProcedureSpecStmt node) {
        endNode(node);
    }

    public void postVisit(TRaiseStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlRecordTypeDefStmt node) {
        endNode(node);
    }

    public void postVisit(TReturnStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlSqlStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlSubProgram node) {
        endNode(node);
    }

    public void postVisit(TPlsqlTableTypeDefStmt node) {
        endNode(node);
    }

    public void postVisit(TVarDeclStmt node) {
        endNode(node);
    }

    public void postVisit(TPlsqlVarrayTypeDefStmt node) {
        endNode(node);
    }

    public void postVisit(TSqlplusCmdStatement node) {
        endNode(node);
    }

    public void preVisit(TCursorDeclStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlDummyStmt node) {
        addNode(node);
    }

    public void preVisit(TElsifStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlExecImmeStmt node) {
        addNode(node);
    }

    public void preVisit(TExitStmt node) {
        addNode(node);
    }

    public void preVisit(TFetchStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlForallStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlGotoStmt node) {
        addNode(node);
    }

    public void preVisit(TIfStmt node) {
        addNode(node);
    }

    public void preVisit(TLoopStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlNullStmt node) {
        addNode(node);
    }

    public void preVisit(TOpenforStmt node) {
        addNode(node);
    }

    public void preVisit(TOpenStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlPipeRowStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlPragmaDeclStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlProcedureSpecStmt node) {
        addNode(node);
    }

    public void preVisit(TRaiseStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlRecordTypeDefStmt node) {
        addNode(node);
    }

    public void preVisit(TReturnStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlSqlStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlSubProgram node) {
        addNode(node);
    }

    public void preVisit(TPlsqlTableTypeDefStmt node) {
        addNode(node);
    }

    public void preVisit(TVarDeclStmt node) {
        addNode(node);
    }

    public void preVisit(TPlsqlVarrayTypeDefStmt node) {
        addNode(node);
    }

    public void preVisit(TSqlplusCmdStatement node) {
        addNode(node);
    }

    public void preVisit(TSelectSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TSelectSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TAlterTableStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TAlterTableStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TCreateIndexSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TCreateIndexSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TCreateTableSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TCreateTableSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TTypeName node) {
        addNode(node);
    }

    public void postVisit(TTypeName node) {
        endNode(node);
    }

    public void preVisit(TTypeNameList node) {
        addNode(node);
    }

    public void postVisit(TTypeNameList node) {
        endNode(node);
    }

    public void preVisit(TColumnDefinition node) {
        addNode(node);
    }

    public void postVisit(TColumnDefinition node) {
        endNode(node);
    }

    public void preVisit(TColumnDefinitionList node) {
        addNode(node);
    }

    public void postVisit(TColumnDefinitionList node) {
        endNode(node);
    }

    public void preVisit(TConstraint node) {
        addNode(node);
    }

    public void postVisit(TConstraint node) {
        endNode(node);
    }

    public void preVisit(TConstraintList node) {
        addNode(node);
    }

    public void postVisit(TConstraintList node) {
        endNode(node);
    }

    public void preVisit(TMultiTarget node) {
        addNode(node);
    }

    public void postVisit(TMultiTarget node) {
        endNode(node);
    }

    public void preVisit(TMultiTargetList node) {
        addNode(node);
    }

    public void postVisit(TMultiTargetList node) {
        endNode(node);
    }

    public void preVisit(TFunctionCall node) {
        addNode(node);
    }

    public void postVisit(TFunctionCall node) {
        endNode(node);
    }

    public void preVisit(TOutputClause node) {
        addNode(node);
    }

    public void postVisit(TOutputClause node) {
        endNode(node);
    }

    public void preVisit(TReturningClause node) {
        addNode(node);
    }

    public void postVisit(TReturningClause node) {
        endNode(node);
    }

    public void preVisit(TCreateViewSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TCreateViewSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TDropIndexSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TDropIndexSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TDropTableSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TDropTableSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TDropViewSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TDropViewSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TMergeSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TMergeSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TUnknownSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TUnknownSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TDeleteSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TDeleteSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TUpdateSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TUpdateSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TInsertSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TInsertSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlCommit stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlCommit stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlRollback stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlRollback stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlSaveTran stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlSaveTran stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlBlock stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlBlock stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlCreateProcedure stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlCreateProcedure stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlCreateFunction stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlCreateFunction stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlCreateTrigger stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlCreateTrigger stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlBulkInsert stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlBulkInsert stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlUpdateText stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlUpdateText stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlDeclare stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlDeclare stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlReturn stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlReturn stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlIfElse stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlIfElse stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlPrint stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlPrint stmt) {
        endNode(stmt);
    }

    public void preVisit(TUseDatabase stmt) {
        addNode(stmt);
    }

    public void postVisit(TUseDatabase stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlGo stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlGo stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlContinue stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlContinue stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlGrant stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlGrant stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlFetch stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlFetch stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlClose stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlClose stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlOpen stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlOpen stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlDeallocate stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlDeallocate stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlExecute stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlExecute stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlExecuteAs stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlExecuteAs stmt) {
        endNode(stmt);
    }

    public void preVisit(TBeginTran stmt) {
        addNode(stmt);
    }

    public void postVisit(TBeginTran stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlRaiserror stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlRaiserror stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlLabel stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlLabel stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlGoTo stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlGoTo stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlRevert stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlRevert stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlEndConversation stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlEndConversation stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlBeginDialog stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlBeginDialog stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlSendOnConversation stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlSendOnConversation stmt) {
        endNode(stmt);
    }

    public void preVisit(TMssqlStmtStub stmt) {
        addNode(stmt);
    }

    public void postVisit(TMssqlStmtStub stmt) {
        endNode(stmt);
    }

    public void preVisit(TIntervalExpression stmt) {
        addNode(stmt);
    }

    public void postVisit(TIntervalExpression stmt) {
        endNode(stmt);
    }

    public void preVisit(TQualifyClause stmt) {
        addNode(stmt);
    }

    public void postVisit(TQualifyClause stmt) {
        endNode(stmt);
    }

    public void preVisit(TCreateMaterializedSqlStatement stmt) {
        addNode(stmt);
    }

    public void postVisit(TCreateMaterializedSqlStatement stmt) {
        endNode(stmt);
    }

    public void preVisit(TLimitClause node) {
        addNode(node);
    }

    public void postVisit(TLimitClause snode) {
        endNode(snode);
    }

    public void preVisit(TMySQLDeclare stmt) {
        addNode(stmt);
    }

    public void postVisit(TMySQLDeclare stmt) {
        endNode(stmt);
    }

    public void preVisit(TMySQLCreateFunction stmt) {
        addNode(stmt);
    }

    public void postVisit(TMySQLCreateFunction stmt) {
        endNode(stmt);
    }

    public void preVisit(TMySQLCreateProcedure stmt) {
        addNode(stmt);
    }

    public void postVisit(TMySQLCreateProcedure stmt) {
        endNode(stmt);
    }

    public void preVisit(TMySQLCreateTrigger stmt) {
        addNode(stmt);
    }

    public void postVisit(TMySQLCreateTrigger stmt) {
        endNode(stmt);
    }

    public void preVisit(TValueRowItem node) {
        addNode(node);
    }

    public void postVisit(TValueRowItem node) {
        endNode(node);
    }

    public void preVisit(TValueRowItemList node) {
        addNode(node);
    }

    public void postVisit(TValueRowItemList node) {
        endNode(node);
    }

    public void preVisit(TValueClause node) {
        addNode(node);
    }

    public void postVisit(TValueClause node) {
        endNode(node);
    }

    public void preVisit(TDb2StmtStub node) {
        addNode(node);
    }

    public void postVisit(TDb2StmtStub node) {
        endNode(node);
    }

    public void preVisit(TMySQLStmtStub node) {
        addNode(node);
    }

    public void postVisit(TMySQLStmtStub node) {
        endNode(node);
    }

    public void preVisit(TTrimArgument node) {
        addNode(node);
    }

    public void postVisit(TTrimArgument node) {
        endNode(node);
    }

    public void preVisit(TLockingClause node) {
        addNode(node);
    }

    public void postVisit(TLockingClause node) {
        endNode(node);
    }

    public void preVisit(TLockingClauseList node) {
        addNode(node);
    }

    public void postVisit(TLockingClauseList node) {
        endNode(node);
    }

    public void preVisit(TParameterDeclaration node) {
        addNode(node);
    }

    public void postVisit(TParameterDeclaration node) {
        endNode(node);
    }

    public void preVisit(TParameterDeclarationList node) {
        addNode(node);
    }

    public void postVisit(TParameterDeclarationList node) {
        endNode(node);
    }

    public void preVisit(TViewAliasClause node) {
        addNode(node);
    }

    public void postVisit(TViewAliasClause node) {
        endNode(node);
    }

    public void preVisit(TViewAliasItem node) {
        addNode(node);
    }

    public void postVisit(TViewAliasItem node) {
        endNode(node);
    }

    public void preVisit(TViewAliasItemList node) {
        addNode(node);
    }

    public void postVisit(TViewAliasItemList node) {
        endNode(node);
    }

    public void preVisit(TPTNodeList node) {
        addNode(node);
    }

    public void postVisit(TPTNodeList node) {
        endNode(node);
    }

    public void preVisit(TMdxSelect node) {
        addNode(node);
    }

    public void postVisit(TMdxSelect node) {
        endNode(node);
    }

    public void preVisit(TMdxAxisNode node) {
        addNode(node);
    }

    public void postVisit(TMdxAxisNode node) {
        endNode(node);
    }

    public void preVisit(TMdxExpNode node) {
        addNode(node);
    }

    public void postVisit(TMdxExpNode node) {
        endNode(node);
    }

    public void preVisit(TMdxBinOpNode node) {
        addNode(node);
    }

    public void postVisit(TMdxBinOpNode node) {
        endNode(node);
    }

    public void preVisit(TMdxCalcPropNode node) {
        addNode(node);
    }

    public void postVisit(TMdxCalcPropNode node) {
        endNode(node);
    }

    public void preVisit(TMdxCaseNode node) {
        addNode(node);
    }

    public void postVisit(TMdxCaseNode node) {
        endNode(node);
    }

    public void preVisit(TMdxEmptyNode node) {
        addNode(node);
    }

    public void postVisit(TMdxEmptyNode node) {
        endNode(node);
    }

    public void preVisit(TMdxFloatConstNode node) {
        addNode(node);
    }

    public void postVisit(TMdxFloatConstNode node) {
        endNode(node);
    }

    public void preVisit(TMdxFunctionNode node) {
        addNode(node);
    }

    public void postVisit(TMdxFunctionNode node) {
        endNode(node);
    }

    public void preVisit(TMdxIdentifierNode node) {
        addNode(node);
    }

    public void postVisit(TMdxIdentifierNode node) {
        endNode(node);
    }

    public void preVisit(TMdxIntegerConstNode node) {
        addNode(node);
    }

    public void postVisit(TMdxIntegerConstNode node) {
        endNode(node);
    }

    public void preVisit(TMdxNonEmptyNode node) {
        addNode(node);
    }

    public void postVisit(TMdxNonEmptyNode node) {
        endNode(node);
    }

    public void preVisit(TMdxPropertyNode node) {
        addNode(node);
    }

    public void postVisit(TMdxPropertyNode node) {
        endNode(node);
    }

    public void preVisit(TMdxSetNode node) {
        addNode(node);
    }

    public void postVisit(TMdxSetNode node) {
        endNode(node);
    }

    public void preVisit(TMdxStringConstNode node) {
        addNode(node);
    }

    public void postVisit(TMdxStringConstNode node) {
        endNode(node);
    }

    public void preVisit(TMdxTupleNode node) {
        addNode(node);
    }

    public void postVisit(TMdxTupleNode node) {
        endNode(node);
    }

    public void preVisit(TMdxUnaryOpNode node) {
        addNode(node);
    }

    public void postVisit(TMdxUnaryOpNode node) {
        endNode(node);
    }

    public void preVisit(TMdxWhenNode node) {
        addNode(node);
    }

    public void postVisit(TMdxWhenNode node) {
        endNode(node);
    }

    public void preVisit(TMdxWhereNode node) {
        addNode(node);
    }

    public void postVisit(TMdxWhereNode node) {
        endNode(node);
    }

    public void preVisit(TMdxWithNode node) {
        addNode(node);
    }

    public void postVisit(TMdxWithNode node) {
        endNode(node);
    }

    public void preVisit(TMdxCreateMember node) {
        addNode(node);
    }

    public void postVisit(TMdxCreateMember node) {
        endNode(node);
    }

    public void preVisit(TMdxCreateSubCube node) {
        addNode(node);
    }

    public void postVisit(TMdxCreateSubCube node) {
        endNode(node);
    }

    public void preVisit(TMdxDrillthrough node) {
        addNode(node);
    }

    public void postVisit(TMdxDrillthrough node) {
        endNode(node);
    }

    public void preVisit(TMdxCall node) {
        addNode(node);
    }

    public void postVisit(TMdxCall node) {
        endNode(node);
    }

    public void preVisit(TMdxDimensionNode node) {
        addNode(node);
    }

    public void postVisit(TMdxDimensionNode node) {
        endNode(node);
    }

    public void preVisit(TMdxMeasureNode node) {
        addNode(node);
    }

    public void postVisit(TMdxMeasureNode node) {
        endNode(node);
    }

    public void preVisit(TMdxGroupNode node) {
        addNode(node);
    }

    public void postVisit(TMdxGroupNode node) {
        endNode(node);
    }

    public void preVisit(TMdxLevelNode node) {
        addNode(node);
    }

    public void postVisit(TMdxLevelNode node) {
        endNode(node);
    }

    public void preVisit(TMdxMemberNode node) {
        addNode(node);
    }

    public void postVisit(TMdxMemberNode node) {
        endNode(node);
    }

    public void preVisit(TMdxDimContentNode node) {
        addNode(node);
    }

    public void postVisit(TMdxDimContentNode node) {
        endNode(node);
    }

    public void preVisit(TMdxLevelContentNode node) {
        addNode(node);
    }

    public void postVisit(TMdxLevelContentNode node) {
        endNode(node);
    }

    public void preVisit(TMdxCreateSessionCube node) {
        addNode(node);
    }

    public void postVisit(TMdxCreateSessionCube node) {
        endNode(node);
    }

    public void preVisit(TMdxScope node) {
        addNode(node);
    }

    public void postVisit(TMdxScope node) {
        endNode(node);
    }

    public void preVisit(TMdxExpression node) {
        addNode(node);
    }

    public void postVisit(TMdxExpression node) {
        endNode(node);
    }

    public void preVisit(TCommentOnSqlStmt node) {
        addNode(node);
    }

    public void postVisit(TCommentOnSqlStmt node) {
        endNode(node);
    }

    public void preVisit(TOracleExecuteProcedure node) {
        addNode(node);
    }

    public void postVisit(TOracleExecuteProcedure node) {
        endNode(node);
    }

    public void preVisit(TArrayAccess node) {
        addNode(node);
    }

    public void postVisit(TArrayAccess node) {
        endNode(node);
    }

    public void preVisit(TGroupingSet node) {
        addNode(node);
    }

    public void postVisit(TGroupingSet node) {
        endNode(node);
    }

    public void preVisit(TGroupingSetItem node) {
        addNode(node);
    }

    public void postVisit(TGroupingSetItem node) {
        endNode(node);
    }

    public void preVisit(TRollupCube node) {
        addNode(node);
    }

    public void postVisit(TRollupCube node) {
        endNode(node);
    }

    public void preVisit(TTruncateStatement node) {
        addNode(node);
    }

    public void postVisit(TTruncateStatement node) {
        endNode(node);
    }

    public void preVisit(TAlterSessionStatement node) {
        addNode(node);
    }

    public void postVisit(TAlterSessionStatement node) {
        endNode(node);
    }

    public void preVisit(TCreateSynonymStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateSynonymStmt node) {
        endNode(node);
    }

    public void preVisit(TCreateSequenceStmt node) {
        addNode(node);
    }

    public void postVisit(TCreateSequenceStmt node) {
        endNode(node);
    }

    public void preVisit(TSortBy node) {
        addNode(node);
    }

    public void postVisit(TSortBy node) {
        endNode(node);
    }

    public void preVisit(TDistributeBy node) {
        addNode(node);
    }

    public void postVisit(TDistributeBy node) {
        endNode(node);
    }

    public void preVisit(TPartitionByClause node) {
        addNode(node);
    }

    public void postVisit(TPartitionByClause node) {
        endNode(node);
    }

    public void preVisit(TTeradataStmtStub node) {
        addNode(node);
    }

    public void postVisit(TTeradataStmtStub node) {
        endNode(node);
    }

    public void preVisit(TTableHint node) {
        addNode(node);
    }

    public void postVisit(TTableHint node) {
        endNode(node);
    }

    public void preVisit(TDeclareVariable node) {
        addNode(node);
    }

    public void postVisit(TDeclareVariable node) {
        endNode(node);
    }

    public void preVisit(TDeclareVariableList node) {
        addNode(node);
    }

    public void postVisit(TDeclareVariableList node) {
        endNode(node);
    }

    public void preVisit(TMssqlSet node) {
        addNode(node);
    }

    public void postVisit(TMssqlSet node) {
        endNode(node);
    }

    public void preVisit(TExecutePreparedStatement node) {
        addNode(node);
    }

    public void postVisit(TExecutePreparedStatement node) {
        endNode(node);
    }

    public void preVisit(TIndexDefinition node) {
        addNode(node);
    }

    public void postVisit(TIndexDefinition node) {
        endNode(node);
    }

    public void preVisit(TTeradataLockClause node) {
        addNode(node);
    }

    public void postVisit(TTeradataLockClause node) {
        endNode(node);
    }

    public void preVisit(TCollectColumnIndex node) {
        addNode(node);
    }

    public void postVisit(TCollectColumnIndex node) {
        endNode(node);
    }

    public void preVisit(TCollectFromOption node) {
        addNode(node);
    }

    public void postVisit(TCollectFromOption node) {
        endNode(node);
    }

    public void preVisit(TTeradataGive node) {
        addNode(node);
    }

    public void postVisit(TTeradataGive node) {
        endNode(node);
    }

    public void preVisit(TMssqlThrow node) {
        addNode(node);
    }

    public void postVisit(TMssqlThrow node) {
        endNode(node);
    }

    public void preVisit(TColumnAttributes node) {
        addNode(node);
    }

    public void postVisit(TColumnAttributes node) {
        endNode(node);
    }

    private void preOrderTraverse(TExpression expr) {
        addNode(expr);

        if (!expr.isLeaf()) {
            if (expr.getLeftOperand() != null) {
                preOrderTraverse(expr.getLeftOperand());
            }

            if (expr.getRightOperand() != null) {
                preOrderTraverse(expr.getRightOperand());
            }
        }
        endNode(expr);
    }
}

