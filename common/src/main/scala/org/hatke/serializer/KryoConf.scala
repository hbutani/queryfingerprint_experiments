package org.hatke.serializer

import org.hatke.{Logging, QFPConfig, Utils}

import scala.util.control.NonFatal

case class KryoConf(bufferSize: Int,
                    maxBufferSize: Int,
                    referenceTracking: Boolean,
                    registrationRequired: Boolean,
                    useUnsafe: Boolean,
                    usePool: Boolean,
                    kryoRegistries: Seq[KryoRegistry]
                   )

object KryoConf extends Logging {

  private val BUFFER_SIZE_KEY = "serializer_buffer_size_bytes"
  private val MAX_BUFFER_SIZE_KEY = "serializer_max_buffer_size_bytes"
  private val REFERENCE_TRACKING_KEY = "reference_tracking"
  private val REGISTRATION_REQUIRED_KEY = "registration_required"
  private val USE_UNSAFE_KEY = "use_unsafe"
  private val USE_POOL_KEY = "use_pool"
  private val KRYO_REGISTRIES = "class_registries"

  private def resolveAsKryorRegistry(clsNm : String) : KryoRegistry = {
    Utils.classForName(clsNm).getDeclaredConstructor().newInstance().asInstanceOf[KryoRegistry]
  }


  def apply(tblFCfg : QFPConfig): KryoConf = {
    val cfg = tblFCfg.kryoConfig
    KryoConf(
      QFPConfig.get(cfg, BUFFER_SIZE_KEY).get.toInt,
      QFPConfig.get(cfg, MAX_BUFFER_SIZE_KEY).get.toInt,
      QFPConfig.get(cfg, REFERENCE_TRACKING_KEY).get.toBoolean,
      QFPConfig.get(cfg, REGISTRATION_REQUIRED_KEY).get.toBoolean,
      QFPConfig.get(cfg, USE_UNSAFE_KEY).get.toBoolean,
      QFPConfig.get(cfg, USE_POOL_KEY).get.toBoolean,
      QFPConfig.getList(cfg, KRYO_REGISTRIES).map(resolveAsKryorRegistry)
    )
  }

}