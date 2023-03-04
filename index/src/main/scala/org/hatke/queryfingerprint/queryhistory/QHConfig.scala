package org.hatke.queryfingerprint.queryhistory

import com.typesafe.config.Config
import org.hatke.QFPConfig
import org.hatke.queryfingerprint.queryhistory.search.SearchBuilder
import org.hatke.{Utils => HUtils}

case class QHConfig(esConfig : ESConfig, idxConfig : QHIndexConfig, searchConfig : QHSearchConfig)

case class QHIndexConfig(indexName : String)

case class QHSearchConfig(searchBuilderClass : Class[SearchBuilder],
                          featureBoostThreshold : Int)

case class ESConfig(host : String,
                    port : Int,
                    scheme : String,
                    userName : String,
                    password : String,
                    certFile : String,
                    qfpIndexName : String
                   )

object QHConfig {

  private val ES_CONN_KEY = "elastic_conn"
  private val ES_INDEX_KEY = "elastic_index"
  private val SEARCH_KEY = "search"


  def apply(qfpCfg: QFPConfig): QHConfig = {
    val cfg = qfpCfg.queryHistoryConfig
    val esConnConfig = QFPConfig.getChildConfig(cfg, ES_CONN_KEY)
    val esIndexConfig = QFPConfig.getChildConfig(cfg, ES_INDEX_KEY)
    val searchConfig = QFPConfig.getChildConfig(cfg, SEARCH_KEY)

    QHConfig(
      _ESConfig(esConnConfig),
      _QHIndexConfig(esIndexConfig),
      _QHSearchConfig(searchConfig)
    )
  }

  private object _ESConfig {

    private val HOST_KEY = "host"
    private val PORT_KEY = "port"
    private val SCHEME_KEY = "scheme"
    private val USER_KEY = "userName"
    private val PASSWORD_KEY = "password"
    private val CERT_FILE_KEY = "certFile"
    private val QFP_INDEX_NAME_KEY = "qfp_index_name"

    private val HOST_DEFAULT = "localhost"
    private val PORT_DEFAULT = "9200"
    private val SCHEME_DEFAULT = "http"
    private val USER_DEFAULT = "elastic"
    private val PASSWORD_DEFAULT = "s3cret"
    private val CERT_FILE_DEFAULT = System.getProperty("user.home") + "/learn/elastic_search/es_testcontainer_http_ca.crt"
    private val QFP_INDEX_NAME_DEFAULT = "query_fingerprint"

    def apply(cfg: Config): ESConfig = {
      ESConfig(
        QFPConfig.getOrElse(cfg, HOST_KEY, HOST_DEFAULT),
        QFPConfig.getOrElse(cfg, PORT_KEY, PORT_DEFAULT).toInt,
        QFPConfig.getOrElse(cfg, SCHEME_KEY, SCHEME_DEFAULT),
        QFPConfig.getOrElse(cfg, USER_KEY, USER_DEFAULT),
        QFPConfig.getOrElse(cfg, PASSWORD_KEY, PASSWORD_DEFAULT),
        QFPConfig.getOrElse(cfg, CERT_FILE_KEY, CERT_FILE_DEFAULT),
        QFPConfig.getOrElse(cfg, QFP_INDEX_NAME_KEY, QFP_INDEX_NAME_DEFAULT)
      )
    }
  }

  private object _QHIndexConfig {
    private val INDEX_NAME_KEY = "name"
    private val INDEX_NAME_DEFAULT = "query_fingerprint"

    def apply(cfg : Config) : QHIndexConfig = {
      QHIndexConfig(
        QFPConfig.getOrElse(cfg, INDEX_NAME_KEY, INDEX_NAME_DEFAULT),
      )
    }
  }

  private object _QHSearchConfig {
    private val SEARCHBLDR_CLS_KEY = "searchbuilder_class"
    private val FEATURE_BOOST_THRESHOLD_KEY = "feature_boost_threshold"


    def apply(cfg: Config): QHSearchConfig = {
      QHSearchConfig(
        HUtils.classForName[SearchBuilder](QFPConfig.getRequired(cfg, SEARCHBLDR_CLS_KEY)),
        QFPConfig.getRequired(cfg, FEATURE_BOOST_THRESHOLD_KEY).toInt
      )
    }
  }

}

