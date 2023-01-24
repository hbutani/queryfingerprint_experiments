package org.hatke.queryfingerprint.index

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{ObjectMapper, PropertyNamingStrategies}
import com.sksamuel.elastic4s.{ElasticClient, ElasticDsl, RequestFailure, RequestSuccess, Response}
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.cluster.NodeUsageResponse
import com.sksamuel.elastic4s.requests.indexes.{CreateIndexResponse, IndexResponse}
import com.sksamuel.elastic4s.requests.mappings.MappingDefinition
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.{RestClient, RestClientBuilder}
import org.hatke.queryfingerprint.index.QueryFingerprintSample.client

import java.io.{BufferedInputStream, ByteArrayInputStream, FileInputStream}
import java.security.KeyStore
import java.security.cert.{Certificate, CertificateFactory}
import javax.net.ssl.{SSLContext, TrustManagerFactory}

object ESClientUtils extends App {

  def fileBytes(path: String): Array[Byte] = {
    scala.util.Using(new FileInputStream(path)) { fIS => fIS.readAllBytes() }.get
  }

  def createSslContextFromCa(certificateBytes: Array[Byte]): SSLContext = try {
    val factory = CertificateFactory.getInstance("X.509")
    val trustedCa = factory.generateCertificate(new ByteArrayInputStream(certificateBytes))
    val trustStore = KeyStore.getInstance("pkcs12")
    trustStore.load(null, null)
    trustStore.setCertificateEntry("ca", trustedCa)
    val sslContext = SSLContext.getInstance("TLSv1.3")
    val tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    tmfactory.init(trustStore)
    sslContext.init(null, tmfactory.getTrustManagers, null)
    sslContext
  } catch {
    case e: Exception =>
      throw new RuntimeException(e)
  }

  def setupHttpClient(host: String = "localhost",
                      port: Int = 9200,
                      userName: String = "elastic",
                      password: String = "s3cret",
                      certFile: String = System.getProperty("user.home") + "/learn/elastic_search/es_testcontainer_http_ca.crt"

                     ) = {
    val host = new HttpHost("localhost", port, "http")
    val credentialsProvider = new BasicCredentialsProvider
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password))
    val builder = RestClient.builder(host)

    builder.setHttpClientConfigCallback((clientBuilder: HttpAsyncClientBuilder) => {
      //      clientBuilder.setSSLContext(createSslContextFromCa(fileBytes(certFile)))
      clientBuilder.setDefaultCredentialsProvider(credentialsProvider)
      clientBuilder
    })

    val mapper = new ObjectMapper
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    val restClient = builder.build

    ElasticClient(JavaClient.fromRestClient(restClient))

  }

  import com.sksamuel.elastic4s.ElasticDsl._

  val client = setupHttpClient()

  try {
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

    resp foreach (search => println(s"There were ${search.totalHits} total hits"))

    val nodeResp: Response[NodeUsageResponse] = client.execute {
      nodeUsage()
    }.await

    println(s"Node Usage: ${nodeResp.result.toString}")


  } finally {
    client.close()
  }

}
