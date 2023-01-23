package org.hatke.queryfingerprint.index

import com.sksamuel.elastic4s.requests.indexes.{CreateIndexResponse, IndexMappings, IndexResponse}
import org.hatke.queryfingerprint.model.Queryfingerprint
import com.sksamuel.elastic4s.ElasticDsl
import com.sksamuel.elastic4s.fields.ObjectField

object QueryFingerprintSample extends App {

  lazy val client = ESClientUtils.setupHttpClient()

  def deleteIndex() = {
    import ElasticDsl._

    try {
      client.execute {
        ElasticDsl.deleteIndex("query_fingerprint")
      }.await.result
    } catch {
      case _ : Throwable => ()
    }
  }

  def createIndex(): CreateIndexResponse = {
    import ElasticDsl._

    client.execute {
      val req = ElasticDsl.createIndex("query_fingerprint").
        shards(1).
        replicas(1).
        mapping(
          properties(
            keywordField("jsonClass").copy(index = Some(false)),
            keywordField("uuid").copy(index = Some(false)),
            keywordField("tablesReferenced"),
            keywordField("columnsScanned"),
            keywordField("columnsFiltered"),
            keywordField("columnsScanFiltered"),
            keywordField("groupedColumns"),
            keywordField("orderedColumns"),
            ObjectField("predicates", None, None, Seq(
              keywordField("jsonClass").copy(index = Some(false)),
              keywordField("column"),
              keywordField("operator"),
              keywordField("functionApplication")
            )),
            keywordField("scannedPredicates"),
            keywordField("functionApplications"),
            ObjectField("joins", None, None, Seq(
              keywordField("jsonClass").copy(index = Some(false)),
              keywordField("leftTable"),
              keywordField("leftColumns"),
              keywordField("rightTable"),
              keywordField("rightColumns"),
              keywordField("joinType")
            )
            ),
            // keywordField("joins"),
            keywordField("correlatedColumns"),
          ) // .all(false)
        ).
        singleShard().
        singleReplica()

      println(req.toString)

      req

    }.await.result
  }

  def indexQFP(qfp: Queryfingerprint): IndexResponse = {
    import com.sksamuel.elastic4s.ElasticDsl._
    import scala.jdk.CollectionConverters._

    client.execute {

      val tablesReferenced = qfp.getTablesReferenced.asScala.map(t => ("tablesReferenced" -> t))
      val columnsScanned = qfp.getColumnsScanned.asScala.map(c => ("columnsScanned" -> c))
      val columnsFiltered = qfp.getColumnsFiltered.asScala.map(c => ("columnsFiltered" -> c))
      val columnsScanFiltered = qfp.getColumnsScanFiltered.asScala.map(c => ("columnsScanFiltered" -> c))
      val groupedColumns = qfp.getGroupedColumns.asScala.map(c => ("groupedColumns" -> c))
      val orderedColumns = qfp.getOrderedColumns.asScala.map(c => ("orderedColumns" -> c))
      val predicates = qfp.getPredicates.asScala.map(p => ("predicates" -> p))
      val scannedPredicates = qfp.getScanPredicates.asScala.map(p => ("scannedPredicates" -> p))
      val joins = qfp.getJoins.asScala.map(p => ("joins" -> p))
      val correlatedColumns = qfp.getCorrelatedColumns.asScala.map(p => ("correlatedColumns" -> p))

      indexInto("query_fingerprint").fields(
        tablesReferenced ++ columnsScanned ++ columnsFiltered ++
          columnsScanFiltered ++ groupedColumns ++ orderedColumns ++
          predicates ++ scannedPredicates ++ joins ++ correlatedColumns
      ).withId(qfp.getUuid.toString)
    }.await.result
  }

  def indexQFP2(qfp : TestQueryFingerPrint) : IndexResponse = {
    import com.sksamuel.elastic4s.ElasticDsl._
    import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._

    implicit val jF = TestQueryFingerPrint.jsonFormat
    implicit val a = org.json4s.jackson.Serialization

    client.execute {
      indexInto("query_fingerprint").doc(qfp).refreshImmediately
    }.await.result

  }

  def showMapping() : Seq[IndexMappings] = {
    import com.sksamuel.elastic4s.ElasticDsl._
    import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._

    client.execute {
      ElasticDsl.getMapping("query_fingerprint")
    }.await.result
  }

  def searchAll : IndexedSeq[TestQueryFingerPrint] = {
    import com.sksamuel.elastic4s.ElasticDsl._
    import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._

    implicit val jF = TestQueryFingerPrint.jsonFormat
    implicit val a = org.json4s.jackson.Serialization

    val r = client.execute {
      search("query_fingerprint").matchAllQuery()
    }.await.result

    r.to[TestQueryFingerPrint]

  }

  def close() : Unit = {
    client.close()
  }

  try {

    deleteIndex()
    createIndex()

    for(qfp <- TestQueryFingerPrint.tpcdsQFP.values) {
      indexQFP2(qfp)
    }

    println(showMapping().mkString("\n"))

    val rQFPs = searchAll

    println(rQFPs.mkString("\n"))
  } finally {
    close()
  }

}
