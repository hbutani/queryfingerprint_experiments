package org.hatke.queryfingerprint.index

import gudusoft.gsqlparser.EDbVendor
import org.hatke.QFPConfig
import org.hatke.queryfingerprint.QFPEnv
import org.hatke.queryfingerprint.index.fulltext.TPCDSSQLEnv
import org.hatke.queryfingerprint.model.{TpcdsUtils, Queryfingerprint => QFP}
import org.hatke.queryfingerprint.queryhistory.{QueryHistoryIndex, Utils}
import org.hatke.queryfingerprint.snowflake.parse.{QueryAnalysis, QueryfingerprintBuilder}


object QueryFingerprintSample extends App {

  lazy val qfpConfig = new QFPConfig()

  implicit lazy val qfpEnv : QFPEnv = QFPEnv(qfpConfig)


  lazy val qhIndex = new QueryHistoryIndex(qfpEnv)

  private val sqlEnv = new TPCDSSQLEnv(EDbVendor.dbvsnowflake)

  println(s"Elastic Cluster Usage:\n ${qhIndex.nodeUsage()}")

  try {

    qhIndex.deleteIndex()
    qhIndex.createIndex()

    val reverseMap = scala.collection.mutable.Map[String, String]()

    val fpMap : Map[String, QFP] =  (for( id <- 1 until 100
                                          if !Set(5, 9, 12, 14, 16, 21, 23, 24, 32, 37, 39, 40,
                                            50, 62, 64, 77, 80, 82, 92, 94, 95, 98, 99).contains(id)
                                          ) yield {
      val query = TpcdsUtils.readTpcdsQuery(s"query$id")
      val qa = new QueryAnalysis(sqlEnv, query)
      val qfpB = new QueryfingerprintBuilder(qa)
      val fps = qfpB.build

      fps.forEach(qhIndex.index)

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

    println(qhIndex.showMapping().mkString("\n"))

    val rQFPs1 = qhIndex.search(fpMap("query1"), true)
    val rQFPs10 = qhIndex.search(fpMap("query10"), true)
    val rQFPs3 = qhIndex.search(fpMap("query3"), true)

    println("SEARCH Query 1:")
    printResult(rQFPs1)

    println("SEARCH Query 10:")
    printResult(rQFPs10)

    println("SEARCH Query 3:")
    printResult(rQFPs3, true)


  } finally {
    qhIndex.close()
  }
}
