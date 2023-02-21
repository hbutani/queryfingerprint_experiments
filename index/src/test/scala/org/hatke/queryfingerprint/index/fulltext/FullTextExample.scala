package org.hatke.queryfingerprint.index.fulltext

import com.fasterxml.jackson.databind.ObjectMapper
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import com.sksamuel.elastic4s.requests.mappings.MappingDefinition
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import org.hatke.QFPConfig
import org.hatke.queryfingerprint.QFPEnv
import org.hatke.queryfingerprint.index.{ESClientUtils, ESUtils}
import org.hatke.queryfingerprint.model.TpcdsUtils

object FullTextExample extends App {

  private val indexName = "queries"

  lazy val qfpConfig = new QFPConfig()
  implicit lazy val qfpEnv: QFPEnv = QFPEnv(qfpConfig)
  lazy val client = ESClientUtils.setupHttpClient

  createTpcdsQueryIndex()
  private val response: SearchResponse = searchQuery(TpcdsUtils.readTpcdsQuery(s"query10"))
  println(response)


  def createTpcdsQueryIndex(): Unit = {
    ESUtils.deleteIndex(client, indexName)
    ESUtils.createIndex(client, indexName, MappingDefinition())

    (1 to 102).foreach {
      i => index(TpcdsUtils.readTpcdsQuery(s"query$i"), i)
    }
  }

  def index(queryStr: String, id: Int): IndexResponse = {
    import com.sksamuel.elastic4s.ElasticDsl._
    client.execute {
      indexInto(indexName).fields(Map("query" -> queryStr, "id" -> id))
    }.await.result
  }

  def searchQuery(queryStr: String): SearchResponse = {
    import com.sksamuel.elastic4s.ElasticDsl._
    client.execute {
      search(indexName).query(queryStr)
    }.await.result
  }

}
