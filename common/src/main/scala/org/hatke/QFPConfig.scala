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

  private val QUERY_HISTORY_PATH = "query_history"
  private val KRYO_PATH = "kyro"


  private lazy val _config = envSpecific.withFallback(ConfigFactory.load())
  private lazy val _queryHistoryConfig = {
    val c = _config
    c.checkValid(ConfigFactory.defaultReference(), QUERY_HISTORY_PATH)
    c.getConfig(QUERY_HISTORY_PATH)
  }

  private lazy val _kryoConfig = {
    val c = _config
    c.checkValid(ConfigFactory.defaultReference(), KRYO_PATH)
    c.getConfig(KRYO_PATH)
  }

  def queryHistoryConfig : Config = _queryHistoryConfig

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

  def getList(config: Config, path: String): List[String] = {

    import scala.jdk.CollectionConverters._

    if (config.hasPath(path)) {
      config.getStringList(path).asScala.toList
    } else {
      List.empty
    }
  }

  def getRequired(config: Config, path: String): String = {
    config.getString(path)
  }

  def getChildConfig(parent : Config, key : String) : Config = {
    parent.checkValid(ConfigFactory.defaultReference(), key)
    parent.getConfig(key)
  }
}
