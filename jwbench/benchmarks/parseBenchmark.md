Benchmark                         Mode  Cnt  Score   Error   Units
TestBM.analyseTablesSimpleQuery  thrpt       3.333          ops/ms
TestBM.dataflowSimpleQuery       thrpt       0.383          ops/ms
TestBM.fingerprintSimpleQuery    thrpt       2.998          ops/ms
TestBM.parseSimpleQuery          thrpt       3.579          ops/ms
TestBM.textFPSimpleQuery         thrpt       3.522          ops/ms
TestBM.analyseTablesSimpleQuery   avgt       0.288           ms/op
TestBM.dataflowSimpleQuery        avgt       2.778           ms/op
TestBM.fingerprintSimpleQuery     avgt       0.334           ms/op
TestBM.parseSimpleQuery           avgt       0.278           ms/op
TestBM.textFPSimpleQuery          avgt       0.282           ms/op

With cache
Benchmark                                  Mode  Cnt  Score   Error   Units
BenchmarkParser.analyseTablesSimpleQuery  thrpt       3.584          ops/ms
BenchmarkParser.dataflowSimpleQuery       thrpt       0.403          ops/ms
BenchmarkParser.fingerprintSimpleQuery    thrpt       3.039          ops/ms
BenchmarkParser.parseSimpleQuery          thrpt       3.472          ops/ms
BenchmarkParser.textFPSimpleQuery         thrpt       3.540          ops/ms
BenchmarkParser.analyseTablesSimpleQuery   avgt       0.282           ms/op
BenchmarkParser.dataflowSimpleQuery        avgt       2.470           ms/op
BenchmarkParser.fingerprintSimpleQuery     avgt       0.337           ms/op
BenchmarkParser.parseSimpleQuery           avgt       0.294           ms/op
BenchmarkParser.textFPSimpleQuery          avgt       0.286           ms/op


Without Tables in env
Benchmark                         Mode  Cnt  Score   Error   Units
TestBM.analyseTablesSimpleQuery  thrpt       3.590          ops/ms
TestBM.dataflowSimpleQuery       thrpt       0.412          ops/ms
TestBM.fingerprintSimpleQuery    thrpt       3.159          ops/ms
TestBM.parseSimpleQuery          thrpt       3.656          ops/ms
TestBM.textFPSimpleQuery         thrpt       3.535          ops/ms
TestBM.analyseTablesSimpleQuery   avgt       0.283           ms/op
TestBM.dataflowSimpleQuery        avgt       2.460           ms/op
TestBM.fingerprintSimpleQuery     avgt       0.319           ms/op
TestBM.parseSimpleQuery           avgt       0.279           ms/op
TestBM.textFPSimpleQuery          avgt       0.282           ms/op