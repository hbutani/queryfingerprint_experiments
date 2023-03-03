package org.hatke.queryfingerprint.queryhistory

import org.hatke.QFPConfig

case class ESConfig(host : String,
                    port : Int,
                    scheme : String,
                    userName : String,
                    password : String,
                    certFile : String) {

}

object ESConfig {

  private val HOST_KEY = "host"
  private val PORT_KEY = "port"
  private val SCHEME_KEY = "scheme"
  private val USER_KEY = "userName"
  private val PASSWORD_KEY = "password"
  private val CERT_FILE_KEY = "certFile"

  private val HOST_DEFAULT = "localhost"
  private val PORT_DEFAULT = "9200"
  private val SCHEME_DEFAULT = "http"
  private val USER_DEFAULT = "elastic"
  private val PASSWORD_DEFAULT = "s3cret"
  private val CERT_FILE_DEFAULT = System.getProperty("user.home") + "/learn/elastic_search/es_testcontainer_http_ca.crt"

  def apply(qfpCfg: QFPConfig): ESConfig = {
    val cfg = qfpCfg.elasticConnConfig
    ESConfig(
      QFPConfig.getOrElse(cfg, HOST_KEY, HOST_DEFAULT),
      QFPConfig.getOrElse(cfg, PORT_KEY, PORT_DEFAULT).toInt,
      QFPConfig.getOrElse(cfg, SCHEME_KEY, SCHEME_DEFAULT),
      QFPConfig.getOrElse(cfg, USER_KEY, USER_DEFAULT),
      QFPConfig.getOrElse(cfg, PASSWORD_KEY, PASSWORD_DEFAULT),
      QFPConfig.getOrElse(cfg, CERT_FILE_KEY, CERT_FILE_DEFAULT)
    )
  }
}
