package org.hatke

import com.typesafe.config.{Config, ConfigFactory}

/**
 * Config is read based on the following preference:
 *  - system properties
 *  - application.conf (all resources on classpath with this name)
 *  - application.json (all resources on classpath with this name)
 *  - application.properties (all resources on classpath with this name)
 *  - reference.conf (all resources on classpath with this name)
 *
 * default `reference.conf` is bundled in jar.Conf has the following structure:
 *
 * {{{
elasticconn {
    host = "localhost",
    port = 9200,
    scheme = "http",
    userName = "elastic",
    password = "s3cret",
    certFile = ""
}

search {
    fingerprint {
        searchbuilder_class = "org.hatke.queryfingerprint.search.FirstSearchDesign"
        feature_boost_threshold = 5
    }

    keyword {
    }
}

kyro {
  serializer_buffer_size_bytes = 65536  // 64K
  serializer_max_buffer_size_bytes = 67108864  // 64M
  reference_tracking = true
  registration_required = false
  use_unsafe = false
  use_pool = true
}

 * }}}
 */
class QFPConfig(envSpecific : Config = ConfigFactory.empty) {

  private val ELASTIC_CONN_PATH = "elasticconn"
  private val SEARCH_PATH = "search"
  private val KRYO_PATH = "kyro"

  val FINGERPRINT_SEARCH_PATH = "fingerprint"
  val KEYWORD_SEARCH_PATH = "keyword"

  private lazy val _config = envSpecific.withFallback(ConfigFactory.load())
  private lazy val _elasticConnConfig = {
    val c = _config
    c.checkValid(ConfigFactory.defaultReference(), ELASTIC_CONN_PATH)
    c.getConfig(ELASTIC_CONN_PATH)
  }

  private lazy val _searchConfig = {
    val c = _config
    c.checkValid(ConfigFactory.defaultReference(), SEARCH_PATH)
    c.getConfig(SEARCH_PATH)
  }

  private lazy val _fingerprintSearchConfig = {
    val c = searchConfig
    c.checkValid(ConfigFactory.defaultReference(), FINGERPRINT_SEARCH_PATH)
    c.getConfig(FINGERPRINT_SEARCH_PATH)
  }

  private lazy val _keywordSearchConfig = {
    val c = searchConfig
    c.checkValid(ConfigFactory.defaultReference(), KEYWORD_SEARCH_PATH)
    c.getConfig(KEYWORD_SEARCH_PATH)
  }

  private lazy val _kryoConfig = {
    val c = _config
    c.checkValid(ConfigFactory.defaultReference(), KRYO_PATH)
    c.getConfig(KRYO_PATH)
  }

  def elasticConnConfig : Config = _elasticConnConfig

  def searchConfig: Config = _searchConfig

  def fingerprintSearchConfig: Config = _fingerprintSearchConfig

  def keywordSearchConfig: Config = _keywordSearchConfig

  def kryoConfig : Config = _kryoConfig


}

object QFPConfig {

  def get(config: Config, path: String): Option[String] = {
    Option(getOrElse(config, path, null))
  }

  def getOrElse(config: Config, path: String, default: String): String = {
    if (config.hasPathOrNull(path)) {
      if (config.getIsNull(path)) {
        default
      } else {
        config.getString(path)
      }
    } else {
      default
    }
  }
}
