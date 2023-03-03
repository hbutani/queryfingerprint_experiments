package org.hatke.serializer

import com.esotericsoftware.kryo.Kryo
import org.hatke.{Logging, Utils}

import scala.util.control.NonFatal

trait KryoRegistry extends Logging {

  def classesToRegister : Seq[Class[_]]

  def registerClasses(kryo : Kryo) : Unit = {

      for(cls <- classesToRegister) {
        try {
          kryo.register(cls)
        } catch {
          case NonFatal(_) => log.warn(s"Failed to register class ${cls} in Kryo.")
        }
      }
  }
}
