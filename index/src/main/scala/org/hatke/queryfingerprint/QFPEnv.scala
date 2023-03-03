package org.hatke.queryfingerprint

import org.hatke.QFPConfig
import org.hatke.queryfingerprint.queryhistory.ESConfig
import org.hatke.serializer.{createKryoSerializer, KryoConf, KryoSerializer}

case class QFPEnv private (config : QFPConfig) {

  lazy val esconnConfig : ESConfig = ESConfig(config)

  lazy val kryoConf: KryoConf = KryoConf(config)

  lazy val kryoSerializer: KryoSerializer = createKryoSerializer(kryoConf)

}

object QFPEnv {

  def apply(config : QFPConfig) : QFPEnv = {
    new QFPEnv(config)
  }
}
