package org.hatke.queryfingerprint

import org.hatke.QFPConfig
import org.hatke.queryfingerprint.index.ESConfig

case class QFPEnv private (config : QFPConfig) {

  lazy val esconnConfig : ESConfig = ESConfig(config)

}

object QFPEnv {

  def apply(config : QFPConfig) : QFPEnv = {
    new QFPEnv(config)
  }
}
