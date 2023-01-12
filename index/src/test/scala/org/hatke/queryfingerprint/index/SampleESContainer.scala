package org.hatke.queryfingerprint.index


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{ObjectMapper, PropertyNamingStrategies}
import com.sksamuel.elastic4s.fields.TextField
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess}
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.{Node, NodeSelector, RestClient, RestClientBuilder}
import org.testcontainers.elasticsearch.ElasticsearchContainer

/**
 * How to setup a ElasticSearch TestContainer & operate on it using elastic4s
 *
 *  - TestContainer setup based on https://github.com/spinscale/elasticsearch-rest-client-samples
 *  - Elastic4s: https://github.com/sksamuel/elastic4s
 */

object SampleESContainer extends  App {
  private val IMAGE_NAME =
    "docker.elastic.co/elasticsearch/elasticsearch:8.3.3"
  private val container =
    new ElasticsearchContainer(IMAGE_NAME).withExposedPorts(9200).withPassword("s3cret")

  private val INGEST_NODE_SELECTOR: NodeSelector = (nodes: java.lang.Iterable[Node]) => {
    val iterator: java.util.Iterator[Node] = nodes.iterator
    while (iterator.hasNext) {
      val node: Node = iterator.next
      // roles may be null if we don't know, thus we keep the node in then...
      if (node.getRoles != null && node.getRoles.isIngest == false) iterator.remove()
    }
  }

  container.start()

  try {
    val host = new HttpHost("localhost", container.getMappedPort(9200), "https")
    val credentialsProvider = new BasicCredentialsProvider
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "s3cret"))
    val builder: RestClientBuilder = RestClient.builder(host)

    builder.setHttpClientConfigCallback((clientBuilder: HttpAsyncClientBuilder) => {
      clientBuilder.setSSLContext(container.createSslContextFromCa)
      clientBuilder.setDefaultCredentialsProvider(credentialsProvider)
      clientBuilder
    })

    builder.setNodeSelector(INGEST_NODE_SELECTOR)

    val mapper = new ObjectMapper
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

    val restClient = builder.build

    val client = ElasticClient(JavaClient.fromRestClient(restClient))

    try {

      import com.sksamuel.elastic4s.ElasticDsl._

      // Next we create an index in advance ready to receive documents.
      // await is a helper method to make this operation synchronous instead of async
      // You would normally avoid doing this in a real program as it will block
      // the calling thread but is useful when testing
      client.execute {
        createIndex("artists").mapping(
          properties(
            TextField("name")
          )
        )
      }.await

      // Next we index a single document which is just the name of an Artist.
      // The RefreshPolicy.Immediate means that we want this document to flush to the disk immediately.
      // see the section on Eventual Consistency.
      client.execute {
        indexInto("artists").fields("name" -> "L.S. Lowry").refresh(RefreshPolicy.Immediate)
      }.await

      // now we can search for the document we just indexed
      val resp = client.execute {
        search("artists").query("lowry")
      }.await

      // resp is a Response[+U] ADT consisting of either a RequestFailure containing the
      // Elasticsearch error details, or a RequestSuccess[U] that depends on the type of request.
      // In this case it is a RequestSuccess[SearchResponse]

      println("---- Search Results ----")
      resp match {
        case failure: RequestFailure => println("We failed " + failure.error)
        case results: RequestSuccess[SearchResponse] => println(results.result.hits.hits.toList)
      }

      // Response also supports familiar combinators like map / flatMap / foreach:
      resp foreach (search => println(s"There were ${search.totalHits} total hits"))

    } finally {
      client.close()
    }
  } finally {
    container.close()
  }
}
