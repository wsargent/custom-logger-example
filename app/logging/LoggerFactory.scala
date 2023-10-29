package logging

import com.tersesystems.echopraxia.spi.{Caller, CoreLoggerFactory};

/**
 * LoggerFactory for a logger.
 */
object LoggerFactory {

  type FB = AppFieldBuilder.type

  val fieldBuilder: FB = AppFieldBuilder

  val FQCN: String = classOf[Logger[_]].getName

  def getLogger(name: String): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    Logger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    Logger(core, fieldBuilder)
  }

  def getLogger[FB](name: String, fieldBuilder: FB): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    Logger(core, fieldBuilder)
  }

  def getLogger[FB](clazz: Class[_], fieldBuilder: FB): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    Logger(core, fieldBuilder)
  }

  def getLogger: Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    Logger(core, fieldBuilder)
  }

  def getLogger[FB](fieldBuilder: FB): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    Logger(core, fieldBuilder)
  }

}
