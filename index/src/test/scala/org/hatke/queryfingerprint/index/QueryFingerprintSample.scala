package org.hatke.queryfingerprint.index

import com.sksamuel.elastic4s.ElasticDsl
import com.sksamuel.elastic4s.requests.indexes.{CreateIndexResponse, IndexMappings, IndexResponse}
import gudusoft.gsqlparser.EDbVendor
import org.hatke.QFPConfig
import org.hatke.queryfingerprint.index.fulltext.TPCDSSQLEnv
import org.hatke.queryfingerprint.model.{TpcdsUtils, Queryfingerprint => QFP}
import org.hatke.queryfingerprint.queryhistory.{ESClientUtils, Utils, QueryFingerprint => QFPIndex, search => srch}
import org.hatke.queryfingerprint.snowflake.parse.{QueryAnalysis, QueryfingerprintBuilder}
import org.hatke.queryfingerprint.QFPEnv
import org.hatke.queryfingerprint.queryhistory.search.FirstSearchDesign


object QueryFingerprintSample extends App {

  import QFPIndex._

  lazy val qfpConfig = new QFPConfig()

  implicit lazy val qfpEnv : QFPEnv = QFPEnv(qfpConfig)

  lazy val client = ESClientUtils.setupHttpClient

  def deleteIndex() = {
    import ElasticDsl._

    try {
      client.execute {
        ElasticDsl.deleteIndex("query_fingerprint")
      }.await.result
    } catch {
      case _: Throwable => ()
    }
  }

  def createIndex(): CreateIndexResponse = {
    import ElasticDsl._

    client.execute {
      val req = ElasticDsl.createIndex("query_fingerprint").
        shards(1).
        replicas(1).
        mapping(QFPIndex.elasticMapping).
        singleShard().
        singleReplica()
      req
    }.await.result
  }

  def indexQFP2(qfp: QFP): IndexResponse = {
    import com.sksamuel.elastic4s.ElasticDsl._

    client.execute {
      indexInto("query_fingerprint").doc(qfp).refreshImmediately
    }.await.result

  }

  def showMapping(): Seq[IndexMappings] = {
    import com.sksamuel.elastic4s.ElasticDsl._

    client.execute {
      ElasticDsl.getMapping("query_fingerprint")
    }.await.result
  }

  def searchAll: IndexedSeq[QFP] = {
    import com.sksamuel.elastic4s.ElasticDsl._

    val r = client.execute {
      search("query_fingerprint").matchAllQuery()
    }.await.result

    r.to[QFP]

  }

  def searchQFP(searchQFP: QFP, explain: Boolean): IndexedSeq[QFP] = {
    srch.search(searchQFP, new FirstSearchDesign, explain)(client)
  }

  def close(): Unit = {
    client.close()
  }

  private val sqlEnv = new TPCDSSQLEnv(EDbVendor.dbvsnowflake)

  try {

    deleteIndex()
    createIndex()

    val reverseMap = scala.collection.mutable.Map[String, String]()

    val fpMap : Map[String, QFP] =  (for( id <- 1 until 100
                                          if !Set(5, 9, 12, 14, 16, 21, 23, 24, 32, 37, 39, 40,
                                            50, 62, 64, 77, 80, 82, 92, 94, 95, 98, 99).contains(id)
                                          ) yield {
      val query = TpcdsUtils.readTpcdsQuery(s"query$id")
      val qa = new QueryAnalysis(sqlEnv, query)
      val qfpB = new QueryfingerprintBuilder(qa)
      val fps = qfpB.build

      fps.forEach(indexQFP2)

      import scala.jdk.CollectionConverters._
      for(fp <- fps.asScala) {
        val qNm = if (fp.getParentQB.isEmpty) s"query$id" else s"Subquery of query$id, hash=${fp.getHash}"
        reverseMap += (fp.getHash.toString -> qNm)
      }

      s"query$id" -> fps.get(0)
    }).toMap



    def getQryNm(qfp : QFP) : String = {

      if (!reverseMap.contains(qfp.getHash.toString)) {
        System.out.println("This shouldn't happen")
      }

      reverseMap.getOrElse(qfp.getHash.toString, "<not recorded subquery>")
    }

    def printResult(rQFPs : Seq[QFP], showExplanation : Boolean = false) : Unit = {
      for(qfp <- rQFPs) {
        val explanation = Utils.asScala(qfp.getExplanation).filter(_ => showExplanation).
          map( e => s"\nExplanation ${e.show}").getOrElse("")
        println(s"""${getQryNm(qfp)}${explanation}""")
      }
    }

    println(showMapping().mkString("\n"))

    val rQFPs1 = searchQFP(fpMap("query1"), true)
    val rQFPs10 = searchQFP(fpMap("query10"), true)
    val rQFPs3 = searchQFP(fpMap("query3"), true)

    println("SEARCH Query 1:")
    printResult(rQFPs1)

    println("SEARCH Query 10:")
    printResult(rQFPs10)

    println("SEARCH Query 3:")
    printResult(rQFPs3, true)

  } finally {
    close()
  }
}
