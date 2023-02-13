package org.hatke.queryfingerprint.index

import com.sksamuel.elastic4s.requests.indexes.CreateIndexResponse
import com.sksamuel.elastic4s.requests.mappings.MappingDefinition
import com.sksamuel.elastic4s.{ElasticClient, ElasticDsl}

object ESUtils {
  def createIndex(esclient: ElasticClient, indexName: String, mappingDef: MappingDefinition): CreateIndexResponse = {
    import ElasticDsl._

    esclient.execute {
      val req = ElasticDsl.createIndex(indexName).
        shards(1).
        replicas(1).
        mapping(mappingDef).
        singleShard().
        singleReplica()
      req
    }.await.result
  }

  def deleteIndex(esClient :ElasticClient, indexName : String) = {
    import ElasticDsl._

    try {
      esClient.execute {
        ElasticDsl.deleteIndex(indexName)
      }.await.result
    } catch {
      case _ : Throwable => ()
    }
  }
}
