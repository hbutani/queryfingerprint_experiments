package org.hatke.serializer

import java.io._
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Output => KryoOutput, UnsafeOutput => KryoUnsafeOutput}
import com.esotericsoftware.kryo.pool.{KryoCallback, KryoFactory, KryoPool}
import com.twitter.chill.{AllScalaRegistrar, EmptyScalaKryoInstantiator}
import org.hatke.Utils

import scala.collection.mutable.ArrayBuffer

trait PoolHandling {
  self: KryoSerializer =>

  private[serializer] class PoolWrapper extends KryoPool {
    private var pool: KryoPool = getPool

    override def borrow(): Kryo = pool.borrow()

    override def release(kryo: Kryo): Unit = pool.release(kryo)

    override def run[T](kryoCallback: KryoCallback[T]): T = pool.run(kryoCallback)

    def reset(): Unit = {
      pool = getPool
    }

    private def getPool: KryoPool = {
      new KryoPool.Builder(self.factory).softReferences.build
    }
  }

  @transient
  private lazy val internalPool = new PoolWrapper

  def pool: KryoPool = internalPool

}

class KryoSerializer(kryoConf : KryoConf) extends Serializable with PoolHandling {

  private val bufferSize = kryoConf.bufferSize
  private val maxBufferSize = kryoConf.maxBufferSize
  private val referenceTracking = kryoConf.referenceTracking
  private val registrationRequired = kryoConf.registrationRequired
  private val useUnsafe = kryoConf.useUnsafe
  private val usePool = kryoConf.usePool

  @volatile protected var defaultClassLoader: Option[ClassLoader] = None

  def newKryoOutput(): KryoOutput =
    if (useUnsafe) {
      new KryoUnsafeOutput(bufferSize, math.max(bufferSize, maxBufferSize))
    } else {
      new KryoOutput(bufferSize, math.max(bufferSize, maxBufferSize))
    }

  @transient
  private[serializer] lazy val factory: KryoFactory = new KryoFactory() {
    override def create: Kryo = {
      newKryo()
    }
  }

  def newKryo(): Kryo = {
    val instantiator = new EmptyScalaKryoInstantiator
    val kryo = instantiator.newKryo()
    kryo.setRegistrationRequired(registrationRequired)

    val classLoader = defaultClassLoader.getOrElse(Thread.currentThread.getContextClassLoader)

    kryo.setReferences(referenceTracking)

    for (cls <- classesToRegister) {
      kryo.register(cls)
    }

    kryo.register(JavaIterableWrapperSerializer.wrapperClass, new JavaIterableWrapperSerializer)

    // Register Chill's classes; we do this after our ranges and the user's own classes to let
    // our code override the generic serializers in Chill for things like Seq
    new AllScalaRegistrar().apply(kryo)

    // Register types missed by Chill.
    // scalastyle:off
    kryo.register(classOf[Array[Tuple1[Any]]])
    kryo.register(classOf[Array[Tuple2[Any, Any]]])
    kryo.register(classOf[Array[Tuple3[Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple4[Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple5[Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple6[Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple7[Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple8[Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple9[Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple10[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple11[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple12[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple13[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple14[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple15[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple16[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple17[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple18[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple19[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple20[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple21[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])
    kryo.register(classOf[Array[Tuple22[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any]]])

    // scalastyle:on

    kryo.register(None.getClass)
    kryo.register(Nil.getClass)
    kryo.register(Utils.classForName("scala.collection.immutable.$colon$colon"))
    kryo.register(Utils.classForName("scala.collection.immutable.Map$EmptyMap$"))
    kryo.register(classOf[ArrayBuffer[Any]])

    qfpClasses.foreach { clazz =>
      kryo.register(clazz)
    }

    kryoConf.kryoRegistries.foreach { kr =>
      kr.registerClasses(kryo)
    }

    kryo.setClassLoader(classLoader)
    kryo
  }

  private[serializer] def newInstance() = {
    new KryoSerializerInstance(this, useUnsafe, usePool)
  }

}
