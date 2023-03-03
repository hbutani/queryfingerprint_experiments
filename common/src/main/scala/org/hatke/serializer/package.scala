package org.hatke

import java.io._
import java.nio.ByteBuffer

import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

import com.esotericsoftware.kryo.{Kryo, Serializer => KryoClassSerializer}
import com.esotericsoftware.kryo.io.{Input => KryoInput, Output => KryoOutput}

/**
 * Kryo based Serialization Infrastructure. Based on how capability is surfaced in Apache Spark.
 * There are 2 public functions:
 *
 *  - [[serializer#serialize()]] to write an Object to a [[InputStream]]
 *  - [[serializer#deserialize()]] to read an object from an [[OutputStream]]
 *
 * These optiionally take a [[KryoSerializer]] that can be constructed using
 * [[serializer#createKryoSerializer()]] passing it a [[KryoConf]]. The default [[KryoSerializer]]
 * instance is setup based on reading the [[Config]] configured for the jvm.
 */
package object serializer {

  private[serializer] val classesToRegister: Seq[Class[_]] = Seq(
    ByteBuffer.allocate(1).getClass,
    classOf[Array[Boolean]],
    classOf[Array[Byte]],
    classOf[Array[Short]],
    classOf[Array[Int]],
    classOf[Array[Long]],
    classOf[Array[Float]],
    classOf[Array[Double]],
    classOf[Array[Char]],
    classOf[Array[String]],
    classOf[Array[Array[String]]]
  )

  private[serializer] val toRegisterSerializer = Map[Class[_], KryoClassSerializer[_]]()

  private[serializer] lazy val qfpClasses: Seq[Class[_]] = Seq(

  )


  /**
   * Bridge KryoInput as an InputStream and ObjectInput.
   * copied from spark project [[KryoInputObjectInputBridge]]
   *
   */
  private[serializer] class KryoInputObjectInputBridge(kryo: Kryo, input: KryoInput)
    extends FilterInputStream(input) with ObjectInput {
    override def readLong(): Long = input.readLong()

    override def readChar(): Char = input.readChar()

    override def readFloat(): Float = input.readFloat()

    override def readByte(): Byte = input.readByte()

    override def readShort(): Short = input.readShort()

    override def readUTF(): String = input.readString() // readString in kryo does utf8
    override def readInt(): Int = input.readInt()

    override def readUnsignedShort(): Int = input.readShortUnsigned()

    override def skipBytes(n: Int): Int = {
      input.skip(n)
      n
    }

    override def readFully(b: Array[Byte]): Unit = input.read(b)

    override def readFully(b: Array[Byte], off: Int, len: Int): Unit = input.read(b, off, len)

    override def readLine(): String = throw new UnsupportedOperationException("readLine")

    override def readBoolean(): Boolean = input.readBoolean()

    override def readUnsignedByte(): Int = input.readByteUnsigned()

    override def readDouble(): Double = input.readDouble()

    override def readObject(): AnyRef = kryo.readClassAndObject(input)
  }

  /**
   * Bridge KryoOutput as an OutputStream and ObjectOutput.
   * copied from spark project [[KryoOutputObjectOutputBridge]]
   */
  private[serializer] class KryoOutputObjectOutputBridge(kryo: Kryo, output: KryoOutput)
    extends FilterOutputStream(output) with ObjectOutput {
    override def writeFloat(v: Float): Unit = output.writeFloat(v)

    // There is no "readChars" counterpart, except maybe "readLine", which is not supported
    override def writeChars(s: String): Unit =
      throw new UnsupportedOperationException("writeChars")

    override def writeDouble(v: Double): Unit = output.writeDouble(v)

    override def writeUTF(s: String): Unit = output.writeString(s) // writeString in kryo does UTF8
    override def writeShort(v: Int): Unit = output.writeShort(v)

    override def writeInt(v: Int): Unit = output.writeInt(v)

    override def writeBoolean(v: Boolean): Unit = output.writeBoolean(v)

    override def write(b: Int): Unit = output.write(b)

    override def write(b: Array[Byte]): Unit = output.write(b)

    override def write(b: Array[Byte], off: Int, len: Int): Unit = output.write(b, off, len)

    override def writeBytes(s: String): Unit = output.writeString(s)

    override def writeChar(v: Int): Unit = output.writeChar(v.toChar)

    override def writeLong(v: Long): Unit = output.writeLong(v)

    override def writeByte(v: Int): Unit = output.writeByte(v)

    override def writeObject(obj: AnyRef): Unit = kryo.writeClassAndObject(output, obj)
  }

  /**
   * A Kryo serializer for serializing results returned by asJavaIterable.
   * copied from spark [[JavaIterableWrapperSerializer]]
   */
  private[serializer] class JavaIterableWrapperSerializer
    extends com.esotericsoftware.kryo.Serializer[java.lang.Iterable[_]] {

    import JavaIterableWrapperSerializer._

    override def write(kryo: Kryo, out: KryoOutput, obj: java.lang.Iterable[_]): Unit = {
      // If the object is the wrapper, simply serialize the underlying Scala Iterable object.
      // Otherwise, serialize the object itself.
      if (obj.getClass == wrapperClass && underlyingMethodOpt.isDefined) {
        kryo.writeClassAndObject(out, underlyingMethodOpt.get.invoke(obj))
      } else {
        kryo.writeClassAndObject(out, obj)
      }
    }

    override def read(kryo: Kryo, in: KryoInput, clz: Class[java.lang.Iterable[_]])
    : java.lang.Iterable[_] = {
      kryo.readClassAndObject(in) match {
        case scalaIterable: Iterable[_] => scalaIterable.asJava
        case javaIterable: java.lang.Iterable[_] => javaIterable
      }
    }
  }

  private[serializer] object JavaIterableWrapperSerializer {
    val wrapperClass = Seq(1).asJava.getClass

    // Get the underlying method so we can use it to get the Scala collection for serialization.
    private val underlyingMethodOpt = {
      try Some(wrapperClass.getDeclaredMethod("underlying")) catch {
        case e: Exception =>
          // logError("Failed to find the underlying field in " + wrapperClass, e)
          None
      }
    }
  }

  def serialize(value : AnyRef,
                outStream : OutputStream)
               (implicit kryoSerializer: KryoSerializer) : Unit = {
    var out: KryoSerializationStream = null
    Utils.tryWithSafeFinally {
      out = kryoSerializer.newInstance().serializeStream(outStream)
      out.writeObject(value)
    } {
      if (out != null) {
        out.close()
      }
    }
  }

  def serialize[T: ClassTag](t: T)
                            (implicit kryoSerializer: KryoSerializer): Array[Byte] = {
    kryoSerializer.newInstance().serialize(t)
  }

  def deserialize[T : ClassTag](inStream : InputStream)
                               ( implicit kryoSerializer: KryoSerializer) : T = {
    var in: KryoDeserializationStream = null
    Utils.tryWithSafeFinally {
      in = kryoSerializer.newInstance().deserializeStream(inStream)
      in.readObject[T]()
    } {
      if (in != null) {
        in.close()
      }
    }
  }

  def deserialize[T: ClassTag](bytes: Array[Byte])
                              (implicit kryoSerializer: KryoSerializer): T =
    kryoSerializer.newInstance().deserialize[T](bytes)

  def createKryoSerializer(kryoConf : KryoConf) : KryoSerializer =
    new KryoSerializer(kryoConf)

}
