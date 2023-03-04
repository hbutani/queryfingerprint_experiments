package org.hatke.queryfingerprint.queryhistory

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{ObjectMapper, PropertyNamingStrategies}
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.http.JavaClient
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.hatke.Logging
import org.hatke.queryfingerprint.QFPEnv

import java.io.{ByteArrayInputStream, FileInputStream}
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.{SSLContext, TrustManagerFactory}

object ESClientUtils extends Logging {

  private def fileBytes(path: String): Array[Byte] = {
    scala.util.Using(new FileInputStream(path)) { fIS => fIS.readAllBytes() }.get
  }

  private def createSslContextFromCa(certificateBytes: Array[Byte]): SSLContext = try {
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

  def setupHttpClient(implicit qfpEnv : QFPEnv) = {
    _setupHttpClient(qfpEnv.qhConfig.esConfig)
  }

  private[queryhistory] def _setupHttpClient(implicit cfg: ESConfig): ElasticClient = {
    _setupHttpClient(cfg.host, cfg.port, cfg.scheme, cfg.userName, cfg.password, cfg.certFile)
  }

  private def _setupHttpClient(_host: String,
                               port: Int,
                               scheme: String,
                               userName: String,
                               password: String,
                               certFile: String
                              ): ElasticClient = {

    val host = new HttpHost(_host, port, scheme)
    val credentialsProvider = new BasicCredentialsProvider
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password))
    val builder = RestClient.builder(host)

    builder.setHttpClientConfigCallback((clientBuilder: HttpAsyncClientBuilder) => {
      if (scheme == "https") {
        clientBuilder.setSSLContext(createSslContextFromCa(fileBytes(certFile)))
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider)
      }
      clientBuilder
    })

    val mapper = new ObjectMapper
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    val restClient = builder.build
    log.info(s"Elastic Client created: ${_host}:${port}; scheme=${scheme}")

    ElasticClient(JavaClient.fromRestClient(restClient))

  }

}
