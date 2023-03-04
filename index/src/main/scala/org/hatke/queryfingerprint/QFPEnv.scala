package org.hatke.queryfingerprint

import org.hatke.QFPConfig
import org.hatke.queryfingerprint.queryhistory.{QHConfig}
import org.hatke.serializer.{KryoConf, KryoSerializer, createKryoSerializer}

case class QFPEnv private (config : QFPConfig) {

  lazy val qhConfig : QHConfig = QHConfig(config)

  lazy val kryoConf: KryoConf = KryoConf(config)

  lazy val kryoSerializer: KryoSerializer = createKryoSerializer(kryoConf)

}

object QFPEnv {

  def apply(config : QFPConfig) : QFPEnv = {
    new QFPEnv(config)
  }
}
