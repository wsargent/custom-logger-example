package logging

import com.tersesystems.echopraxia.spi.{Caller, CoreLoggerFactory}
import sourcecode.{Args, Enclosing, Line};

/**
 * LoggerFactory for a logger.
 */
object LoggerFactory {

  // the base field builder type that we can assume the entire app has access to
  type BaseFieldBuilder = AppFieldBuilder.type

  val fieldBuilder: BaseFieldBuilder = AppFieldBuilder

  private val sourceInfoMethod = (fb: BaseFieldBuilder, line: Line, enc: Enclosing, _: Args) => {
    fb.obj("sourceInfo", Seq(
      fb.keyValue("line" -> line.value),
      fb.keyValue("enclosing" -> enc.value)
    ))
  }

  val FQCN: String = classOf[Logger[_]].getName

  def getLogger(name: String): Logger[BaseFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    Logger(core, fieldBuilder, sourceInfoMethod)
  }

  def getLogger(clazz: Class[_]): Logger[BaseFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    Logger(core, fieldBuilder, sourceInfoMethod)
  }

  def getLogger[FB <: BaseFieldBuilder](name: String, fieldBuilder: FB): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    Logger(core, fieldBuilder, sourceInfoMethod)
  }

  def getLogger[FB <: BaseFieldBuilder](clazz: Class[_], fieldBuilder: FB): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    Logger(core, fieldBuilder, sourceInfoMethod)
  }

  def getLogger: Logger[BaseFieldBuilder] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    Logger(core, fieldBuilder, sourceInfoMethod)
  }

  def getLogger[FB <: BaseFieldBuilder](fieldBuilder: FB): Logger[FB] = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    Logger(core, fieldBuilder, sourceInfoMethod)
  }

}
