package org.hatke.serializer

import java.io.{EOFException, InputStream, IOException, OutputStream}
import java.nio.ByteBuffer
import java.util.Locale

import scala.reflect.ClassTag

import com.esotericsoftware.kryo.{Kryo, KryoException}
import com.esotericsoftware.kryo.io.{ByteBufferInputStream, Input => KryoInput, Output => KryoOutput, UnsafeInput => KryoUnsafeInput, UnsafeOutput => KryoUnsafeOutput}

private[serializer] class KryoSerializerInstance(ks: KryoSerializer,
                                                 useUnsafe: Boolean, usePool: Boolean
                                                ) {
  private[this] var cachedKryo: Kryo = if (usePool) null else borrowKryo()

  private[serializer] def borrowKryo(): Kryo = {
    if (usePool) {
      val kryo = ks.pool.borrow()
      kryo.reset()
      kryo
    } else {
      if (cachedKryo != null) {
        val kryo = cachedKryo
        kryo.reset()
        cachedKryo = null
        kryo
      } else {
        ks.newKryo()
      }
    }
  }

  private[serializer] def releaseKryo(kryo: Kryo): Unit = {
    if (usePool) {
      ks.pool.release(kryo)
    } else {
      if (cachedKryo == null) {
        cachedKryo = kryo
      }
    }
  }

  private lazy val output = ks.newKryoOutput()
  private lazy val input = if (useUnsafe) new KryoUnsafeInput() else new KryoInput()

  def serialize[T: ClassTag](t: T): Array[Byte] = {
    output.clear()
    val kryo = borrowKryo()
    try {
      kryo.writeClassAndObject(output, t)
    } finally {
      releaseKryo(kryo)
    }
    output.toBytes
  }

  def serializeAsBuffer[T: ClassTag](t: T): ByteBuffer = {
    ByteBuffer.wrap(serialize(t))
  }

  def deserialize[T: ClassTag](bytes: Array[Byte]): T = {
    val kryo = borrowKryo()
    try {
      input.setBuffer(bytes, 0, bytes.size)
      kryo.readClassAndObject(input).asInstanceOf[T]
    } finally {
      releaseKryo(kryo)
    }
  }

  def deserialize[T: ClassTag](bytes: ByteBuffer): T = {
    val kryo = borrowKryo()
    try {
      if (bytes.hasArray) {
        input.setBuffer(bytes.array(), bytes.arrayOffset() + bytes.position(), bytes.remaining())
      } else {
        input.setBuffer(new Array[Byte](4096))
        input.setInputStream(new ByteBufferInputStream(bytes))
      }
      kryo.readClassAndObject(input).asInstanceOf[T]
    } finally {
      releaseKryo(kryo)
    }
  }

  def deserialize[T: ClassTag](bytes: ByteBuffer, loader: ClassLoader): T = {
    val kryo = borrowKryo()
    val oldClassLoader = kryo.getClassLoader
    try {
      kryo.setClassLoader(loader)
      if (bytes.hasArray) {
        input.setBuffer(bytes.array(), bytes.arrayOffset() + bytes.position(), bytes.remaining())
      } else {
        input.setBuffer(new Array[Byte](4096))
        input.setInputStream(new ByteBufferInputStream(bytes))
      }
      kryo.readClassAndObject(input).asInstanceOf[T]
    } finally {
      kryo.setClassLoader(oldClassLoader)
      releaseKryo(kryo)
    }
  }

  private[serializer] def serializeStream(s: OutputStream) = {
    new KryoSerializationStream(this, s, useUnsafe)
  }

  private[serializer] def deserializeStream(s: InputStream) = {
    new KryoDeserializationStream(this, s, useUnsafe)
  }

  def getAutoReset(): Boolean = {
    val field = classOf[Kryo].getDeclaredField("autoReset")
    field.setAccessible(true)
    val kryo = borrowKryo()
    try {
      field.get(kryo).asInstanceOf[Boolean]
    } finally {
      releaseKryo(kryo)
    }
  }
}

private[serializer] class KryoDeserializationStream(
                                                     serInstance: KryoSerializerInstance,
                                                     inStream: InputStream,
                                                     useUnsafe: Boolean) {

  private[this] var input: KryoInput =
    if (useUnsafe) new KryoUnsafeInput(inStream) else new KryoInput(inStream)

  private[this] var kryo: Kryo = serInstance.borrowKryo()

  def readObject[T: ClassTag](): T = {
    try {
      kryo.readClassAndObject(input).asInstanceOf[T]
    } catch {
      // DeserializationStream uses the EOF exception to indicate stopping condition.
      case e: KryoException
        if e.getMessage.toLowerCase(Locale.ROOT).contains("buffer underflow") =>
        throw new EOFException
    }
  }

  def close() : Unit = {
    if (input != null) {
      try {
        // Kryo's Input automatically closes the input stream it is using.
        input.close()
      } finally {
        serInstance.releaseKryo(kryo)
        kryo = null
        input = null
      }
    }
  }
}

private[serializer]
class KryoSerializationStream(
                               serInstance: KryoSerializerInstance,
                               outStream: OutputStream,
                               useUnsafe: Boolean) {

  private[this] var output: KryoOutput =
    if (useUnsafe) new KryoUnsafeOutput(outStream) else new KryoOutput(outStream)

  private[this] var kryo: Kryo = serInstance.borrowKryo()

  def writeObject[T: ClassTag](t: T): KryoSerializationStream = {
    kryo.writeClassAndObject(output, t)
    this
  }

  def flush() : Unit = {
    if (output == null) {
      throw new IOException("Stream is closed")
    }
    output.flush()
  }

  def close() : Unit = {
    if (output != null) {
      try {
        output.close()
      } finally {
        serInstance.releaseKryo(kryo)
        kryo = null
        output = null
      }
    }
  }
}

