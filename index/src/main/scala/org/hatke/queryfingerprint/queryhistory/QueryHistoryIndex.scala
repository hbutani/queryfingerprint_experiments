package org.hatke.queryfingerprint.queryhistory

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.requests.cluster.NodeUsageResponse
import com.sksamuel.elastic4s.requests.indexes.{CreateIndexResponse, IndexMappings, IndexResponse}
import org.hatke.queryfingerprint.QFPEnv
import org.hatke.queryfingerprint.model.Queryfingerprint
import org.hatke.queryfingerprint.queryhistory.{search => _search}

class QueryHistoryIndex(qfpEnv: QFPEnv) {

  val qhConfig : QHConfig = qfpEnv.qhConfig
  @transient implicit val elasticConfig : ESConfig = qhConfig.esConfig
  @transient val indexConfig : QHIndexConfig = qhConfig.idxConfig
  @transient val searchConfig : QHSearchConfig = qhConfig.searchConfig

  import QueryFingerprintUtils._

  lazy val client: ElasticClient = ESClientUtils._setupHttpClient

  def deleteIndex() = {
    import ElasticDsl._

    try {
      client.execute {
        ElasticDsl.deleteIndex(indexConfig.indexName)
      }.await.result
    } catch {
      case _: Throwable => ()
    }
  }

  def createIndex(): CreateIndexResponse = {
    import ElasticDsl._

    client.execute {
      val req = ElasticDsl.createIndex(indexConfig.indexName).
        shards(1).
        replicas(1).
        mapping(QueryFingerprintUtils.elasticMapping).
        singleShard().
        singleReplica()
      req
    }.await.result
  }

  def index(qfp: Queryfingerprint): IndexResponse = {
    import com.sksamuel.elastic4s.ElasticDsl._

    client.execute {
      indexInto(indexConfig.indexName).doc(qfp).refreshImmediately
    }.await.result

  }

  def showMapping(): Seq[IndexMappings] = {
    import com.sksamuel.elastic4s.ElasticDsl._

    client.execute {
      ElasticDsl.getMapping(indexConfig.indexName)
    }.await.result
  }

  def search(searchQFP: Queryfingerprint, explain: Boolean): IndexedSeq[Queryfingerprint] = {
    _search.search(searchQFP,
      searchConfig.searchBuilderClass.getDeclaredConstructor().newInstance(),
      explain
    )(client)
  }

  def nodeUsage() : NodeUsageResponse = {
    import ElasticDsl._

    client.execute {
      ElasticDsl.nodeUsage()
    }.await.result
  }

  def close(): Unit = {
    client.close()
  }
}
