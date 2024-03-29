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

import org.apache.log4j.{Level, LogManager, PropertyConfigurator}
import org.slf4j.{Logger, LoggerFactory}
import org.slf4j.impl.StaticLoggerBinder

trait Logging {

  // Make the log field transient so that objects with Logging can
  // be serialized and used on another machine
  @transient private var log_ : Logger = null

  // Method to get the logger name for this object
  protected def logName = {
    // Ignore trailing $'s in the class names for Scala objects
    this.getClass.getName.stripSuffix("$")
  }

  // Method to get or create the logger for this object
  protected def log: Logger = {
    if (log_ == null) {
      initializeLogIfNecessary(false)
      log_ = LoggerFactory.getLogger(logName)
    }
    log_
  }

  // Log methods that take only a String
  def logInfo(msg: => String) : Unit = {
    if (log.isInfoEnabled) log.info(msg)
  }

  def logDebug(msg: => String) : Unit = {
    if (log.isDebugEnabled) log.debug(msg)
  }

  def logTrace(msg: => String) : Unit = {
    if (log.isTraceEnabled) log.trace(msg)
  }

  def logWarning(msg: => String) : Unit = {
    if (log.isWarnEnabled) log.warn(msg)
  }

  def logError(msg: => String) : Unit = {
    if (log.isErrorEnabled) log.error(msg)
  }

  // Log methods that take Throwables (Exceptions/Errors) too
  def logInfo(msg: => String, throwable: Throwable) : Unit = {
    if (log.isInfoEnabled) log.info(msg, throwable)
  }

  def logDebug(msg: => String, throwable: Throwable) : Unit = {
    if (log.isDebugEnabled) log.debug(msg, throwable)
  }

  def logTrace(msg: => String, throwable: Throwable) : Unit = {
    if (log.isTraceEnabled) log.trace(msg, throwable)
  }

  def logWarning(msg: => String, throwable: Throwable) : Unit = {
    if (log.isWarnEnabled) log.warn(msg, throwable)
  }

  def logError(msg: => String, throwable: Throwable) : Unit = {
    if (log.isErrorEnabled) log.error(msg, throwable)
  }

  def isTraceEnabled(): Boolean = {
    log.isTraceEnabled
  }

  protected def initializeLogIfNecessary(isInterpreter: Boolean): Unit = {
    initializeLogIfNecessary(isInterpreter, silent = false)
  }

  protected def initializeLogIfNecessary(
                                          isInterpreter: Boolean,
                                          silent: Boolean = false): Boolean = {
    if (!Logging.initialized) {
      Logging.initLock.synchronized {
        if (!Logging.initialized) {
          initializeLogging(isInterpreter, silent)
          return true
        }
      }
    }
    false
  }

  private def initializeLogging(isInterpreter: Boolean, silent: Boolean): Unit = {
    // Don't use a logger in here, as this is itself occurring during initialization of a logger
    // If Log4j 1.2 is being used, but is not initialized, load a default properties file
    if (Logging.isLog4j12()) {
      val log4j12Initialized = LogManager.getRootLogger.getAllAppenders.hasMoreElements
      // scalastyle:off println
      if (!log4j12Initialized) {
        Logging.defaultLog4jConfig = true
        val defaultLogProps = "log4j.properties"
        Option(getClass.getClassLoader.getResource(defaultLogProps)) match {
          case Some(url) =>
            PropertyConfigurator.configure(url)
            if (!silent) {
              System.err.println(s"Using default log4j profile: $defaultLogProps")
            }
          case None =>
            System.err.println(s"Was unable to load $defaultLogProps")
        }
      }

      val rootLogger = LogManager.getRootLogger()
      if (Logging.defaultRootLevel == null) {
        Logging.defaultRootLevel = rootLogger.getLevel()
      }

      if (isInterpreter) {
        // Use the repl's main class to define the default log level when running the shell,
        // overriding the root logger's config if they're different.
        val replLogger = LogManager.getLogger(logName)
        val replLevel = Option(replLogger.getLevel()).getOrElse(Level.WARN)
        if (replLevel != rootLogger.getEffectiveLevel()) {
          if (!silent) {
            System.err.printf("Setting default log level to \"%s\".\n", replLevel)
            System.err.println("To adjust logging level use sc.setLogLevel(newLevel). ")
          }
          rootLogger.setLevel(replLevel)
        }
      }
      // scalastyle:on println
    }
    Logging.initialized = true

    // Force a call into slf4j to initialize it. Avoids this happening from multiple threads
    // and triggering this: http://mailman.qos.ch/pipermail/slf4j-dev/2010-April/002956.html
    log
  }
}

private[hatke] object Logging {
  @volatile private var initialized = false
  @volatile private var defaultRootLevel: Level = null
  @volatile private var defaultLog4jConfig = false

  val initLock = new Object()
  try {
    // We use reflection here to handle the case where users remove the
    // slf4j-to-jul bridge order to route their logs to JUL.
    val bridgeClass = Utils.classForName("org.slf4j.bridge.SLF4JBridgeHandler")
    bridgeClass.getMethod("removeHandlersForRootLogger").invoke(null)
    val installed = bridgeClass.getMethod("isInstalled").invoke(null).asInstanceOf[Boolean]
    if (!installed) {
      bridgeClass.getMethod("install").invoke(null)
    }
  } catch {
    case e: ClassNotFoundException => // can't log anything yet so just fail silently
  }

  /**
   * Marks the logging system as not initialized. This does a best effort at resetting the
   * logging system to its initial state so that the next class to use logging triggers
   * initialization again.
   */
  def uninitialize(): Unit = initLock.synchronized {
    if (isLog4j12()) {
      if (defaultLog4jConfig) {
        defaultLog4jConfig = false
        LogManager.resetConfiguration()
      } else {
        LogManager.getRootLogger().setLevel(defaultRootLevel)
      }
    }
    this.initialized = false
  }

  private def isLog4j12(): Boolean = {
    // This distinguishes the log4j 1.2 binding, currently
    // org.slf4j.impl.Log4jLoggerFactory, from the log4j 2.0 binding, currently
    // org.apache.logging.slf4j.Log4jLoggerFactory
    val binderClass = StaticLoggerBinder.getSingleton.getLoggerFactoryClassStr
    "org.slf4j.impl.Log4jLoggerFactory".equals(binderClass)
  }
}
