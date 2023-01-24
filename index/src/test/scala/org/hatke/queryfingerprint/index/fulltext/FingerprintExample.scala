package org.hatke.queryfingerprint.index.fulltext

import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import gudusoft.gsqlparser.EDbVendor
import org.hatke.queryfingerprint.index._
import org.hatke.queryfingerprint.index.search.FirstSearchDesign
import org.hatke.queryfingerprint.model.{Queryfingerprint, TpcdsUtils}
import org.hatke.queryfingerprint.snowflake.parse.{QueryAnalysis, QueryfingerprintBuilder}

import java.util.Optional

object FingerprintExample extends App {
  private val indexName = "fingerprint_ex"
  lazy val client = ESClientUtils.setupHttpClient()
  private val sqlEnv = new TPCDSSQLEnv(EDbVendor.dbvsnowflake)

  createTpcdsFingerprintIndex()

  private val response = searchQueryFingerprint(10)
  println(response)

  private def createTpcdsFingerprintIndex() = {
    ESUtils.deleteIndex(client, indexName)
    ESUtils.createIndex(client, indexName, TestQueryFingerPrint.elasticMapping)
    (1 to 102).foreach {
      i => readFingerprint(i).foreach(fp => indexFingerprint(fp))
    }
  }

  private def readFingerprint(id: Int): List[TestQueryFingerPrint] = {
    import scala.jdk.CollectionConverters._
    try {
      val query = TpcdsUtils.readTpcdsQuery(s"query$id")
      val qa = new QueryAnalysis(sqlEnv, query)
      val qfpB = new QueryfingerprintBuilder(qa)
      val fps = qfpB.build
      fps.asScala.map(fp => fingerprintToTestFingerprint(fp, id)).toList
    } catch {
      case e: IllegalArgumentException =>
        List.empty
    }
  }

  private def fingerprintToTestFingerprint(fp: Queryfingerprint, id: Int): TestQueryFingerPrint = {
    import scala.jdk.CollectionConverters._
    val testPredicates = fp.getPredicates.asScala.map {
      p => TestPredicate(p.getColumn, p.getOperator, Option(p.getFunctionName.orElse(null)))
    }.toSet

    val testJoins = fp.getJoins.asScala.map {
      j => TestJoin(j.getLeftTable, j.getLeftColumn, j.getRightTable, j.getRightColumn, TestJoinType.withName(j.getType.toString))
    }.toSet

    val applications = fp.getFunctionApplications.asScala.map {
      fa => TestFunctionApplication(fa.getFunctionName, fa.getColumn)
    }.toSet

    TestQueryFingerPrint(
      uuid = fp.getUuid.toString,
      tablesReferenced = fp.getTablesReferenced.asScala.toSet,
      columnsScanned = fp.getColumnsScanned.asScala.toSet,
      columnsFiltered = fp.getColumnsFiltered.asScala.toSet,
      predicates = testPredicates,
      joins = testJoins,
      functionApplications = applications,
      groupedColumns = fp.getGroupedColumns.asScala.toSet,
      orderedColumns = fp.getOrderedColumns.asScala.toSet,
      id = Some(id)
    )
  }

  private def indexFingerprint(qfp: TestQueryFingerPrint): IndexResponse = {
    import com.sksamuel.elastic4s.ElasticDsl._

    client.execute {
      indexInto(indexName).doc(qfp).refreshImmediately
    }.await.result

  }

  private def searchQueryFingerprint(id: Int): IndexedSeq[TestQueryFingerPrint] = {
    val query = TpcdsUtils.readTpcdsQuery(s"query$id")
    val qa = new QueryAnalysis(sqlEnv, query)
    val qfpB = new QueryfingerprintBuilder(qa)
    val fps = qfpB.build
    import scala.jdk.CollectionConverters._
    val tfp = fingerprintToTestFingerprint(fps.asScala.find(_.getParentQB == Optional.empty()).get, id)
    searchQFP(tfp, false)
  }

  private def searchQFP(searchQFP: TestQueryFingerPrint, explain: Boolean): IndexedSeq[TestQueryFingerPrint] = {
    search.search(searchQFP, new FirstSearchDesign, explain)(client)
  }
}
