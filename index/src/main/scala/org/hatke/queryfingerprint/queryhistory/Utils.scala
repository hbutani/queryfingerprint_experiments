package org.hatke.queryfingerprint.queryhistory

import java.util.Optional

object Utils {

  def asScala[T <: AnyRef](o : Optional[T]) : Option[T] = Option(if (o.isPresent) o.get() else null.asInstanceOf[T])

  def asJava[T <: AnyRef](o : Option[T]) : Optional[T] = Optional.ofNullable(o.getOrElse(null.asInstanceOf[T]))

}
