/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hatke

import java.util.concurrent.atomic.AtomicBoolean

object Utils extends Logging {

  def classForName[C](
                       className: String,
                       initialize: Boolean = true): Class[C] = {
    Class.forName(className, initialize, Thread.currentThread().getContextClassLoader).
      asInstanceOf[Class[C]]
  }

  def tryWithSafeFinally[T](block: => T)(finallyBlock: => Unit): T = {
    var originalThrowable: Throwable = null
    try {
      block
    } catch {
      case t: Throwable =>
        // Purposefully not using NonFatal, because even fatal exceptions
        // we don't want to have our finallyBlock suppress
        originalThrowable = t
        throw originalThrowable
    } finally {
      try {
        finallyBlock
      } catch {
        case t: Throwable if (originalThrowable != null && originalThrowable != t) =>
          originalThrowable.addSuppressed(t)
          throw originalThrowable
      }
    }
  }

  def combine[A](xs: Iterable[Iterable[A]]): Seq[Seq[A]] = {
    xs.foldLeft(Seq(Seq.empty[A])){ (x, y) =>
      for (a <- x; b <- y) yield a :+ b
    }
  }

  val DEFAULT_MAX_TO_STRING_FIELDS = 25

  private def maxNumToStringFields = DEFAULT_MAX_TO_STRING_FIELDS
  private val truncationWarningPrinted = new AtomicBoolean(false)

  def truncatedString[T](
                          seq: Seq[T],
                          start: String,
                          sep: String,
                          end: String,
                          maxNumFields: Int = maxNumToStringFields): String = {
    if (seq.length > maxNumFields) {
      if (truncationWarningPrinted.compareAndSet(false, true)) {
        logWarning(
          "Truncated the string representation of a Plan/Expr since it was too large.")
      }
      val numFields = math.max(0, maxNumFields - 1)
      seq.take(numFields).mkString(
        start, sep, sep + "... " + (seq.length - numFields) + " more fields" + end)
    } else {
      seq.mkString(start, sep, end)
    }
  }

}
