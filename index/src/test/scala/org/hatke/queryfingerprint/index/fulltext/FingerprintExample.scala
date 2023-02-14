package org.hatke.queryfingerprint.index.fulltext

import gudusoft.gsqlparser.EDbVendor
import org.hatke.queryfingerprint.index._
import org.hatke.queryfingerprint.model.{Queryfingerprint, TpcdsUtils}
import org.hatke.queryfingerprint.snowflake.parse.{QueryAnalysis, QueryfingerprintBuilder}

object FingerprintExample extends App {
  private val indexName = "fingerprint_ex"
  lazy val client = ESClientUtils.setupHttpClient()
  private val sqlEnv = new TPCDSSQLEnv(EDbVendor.dbvsnowflake)

//  createTpcdsFingerprintIndex()

//  private val response = searchQueryFingerprint(10)
//  println(response)
  analyzeFingerprints()

  private def analyzeFingerprints(): Unit = {
    val fps = (1 to 102).flatMap { i =>
      readFingerprint(i)
    }.groupBy(_.getHash).view.mapValues(v => (v.size, v)).toList.sortBy(-_._2._1)

    println(fps)
  }

  private def readFingerprint(id : Int) : List[Queryfingerprint] = {
    import scala.jdk.CollectionConverters._
    try {
      val query = TpcdsUtils.readTpcdsQuery(s"query$id")
      val qa = new QueryAnalysis(sqlEnv, query)
      val qfpB = new QueryfingerprintBuilder(qa)
      val fps = qfpB.build
      fps.asScala.toList
    } catch {
      case e: IllegalArgumentException =>
        List.empty
    }
  }
}
