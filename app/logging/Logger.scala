package logging

import com.tersesystems.echopraxia.api.Level._
import com.tersesystems.echopraxia.api.{FieldBuilderResult, Level}
import com.tersesystems.echopraxia.plusscala.api.{Condition, Implicits}
import com.tersesystems.echopraxia.plusscala.spi.{DefaultMethodsSupport, LoggerSupport}
import com.tersesystems.echopraxia.spi.{CoreLogger, Utilities}

import scala.compat.java8.FunctionConverters.enrichAsJavaFunction

import sourcecode._

trait Logger[FB] extends LoggerSupport[FB, Logger] with DefaultMethodsSupport[FB] {

  // TRACE and DEBUG logger methods take source code info

  def trace: DiagnosticLoggerMethod[FB]

  def debug: DiagnosticLoggerMethod[FB]

  def info: LoggerMethod[FB]

  def warn: LoggerMethod[FB]

  def error: LoggerMethod[FB]
}

trait EnabledLoggerMethod {

  /** @return true if the logger enabled at this level. */
  def enabled: Boolean

  /**
   * @param condition
   * the given condition.
   * @return
   * true if the logger level is enabled and the condition is met.
   */
  def enabled(condition: Condition): Boolean

}

trait LoggerMethod[FB] extends EnabledLoggerMethod {

  /**
   * Logs statement if enabled.
   *
   * @param message
   * the message.
   */
  def apply(message: String): Unit

  /**
   * Logs statement if enabled using a field builder function.
   */
  def apply(message: String, f1: FB => FieldBuilderResult): Unit

  /**
   * Logs statement if enabled using two field builder functions.
   */
  def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult): Unit

  /**
   * Logs statement if enabled using three field builder functions.
   */
  def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult): Unit

  /**
   * Logs statement if enabled using four field builder functions.
   */
  def apply(
             message: String,
             f1: FB => FieldBuilderResult,
             f2: FB => FieldBuilderResult,
             f3: FB => FieldBuilderResult,
             f4: FB => FieldBuilderResult
           ): Unit

  /**
   * Logs statement if enabled and condition is met.
   */
  def apply(condition: Condition, message: String): Unit

  /**
   * Logs statement if enabled and condition is met with a field builder function.
   */
  def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult): Unit

  /**
   * Logs statement if enabled and condition is met with two field builder functions.
   */
  def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult): Unit

  /**
   * Logs statement if enabled and condition is met with three field builder functions.
   */
  def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult): Unit

  /**
   * Logs statement if enabled and condition is met with four field builder functions.
   */
  def apply(
             condition: Condition,
             message: String,
             f1: FB => FieldBuilderResult,
             f2: FB => FieldBuilderResult,
             f3: FB => FieldBuilderResult,
             f4: FB => FieldBuilderResult
           ): Unit
}


trait DiagnosticLoggerMethod[FB] extends EnabledLoggerMethod {

  /**
   * Logs statement if enabled.
   *
   * @param message
   * the message.
   */
  def apply(message: String)(implicit line: Line, enc: Enclosing, args: Args): Unit

  /**
   * Logs statement if enabled using a field builder function.
   */
  def apply(message: String, f1: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit

  /**
   * Logs statement if enabled using two field builder functions.
   */
  def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit

  /**
   * Logs statement if enabled using three field builder functions.
   */
  def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit

  /**
   * Logs statement if enabled using four field builder functions.
   */
  def apply(
             message: String,
             f1: FB => FieldBuilderResult,
             f2: FB => FieldBuilderResult,
             f3: FB => FieldBuilderResult,
             f4: FB => FieldBuilderResult
           )(implicit line: Line, enc: Enclosing, args: Args): Unit

  /**
   * Logs statement if enabled and condition is met.
   */
  def apply(condition: Condition, message: String)(implicit line: Line, enc: Enclosing, args: Args): Unit

  /**
   * Logs statement if enabled and condition is met with a field builder function.
   */
  def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit

  /**
   * Logs statement if enabled and condition is met with two field builder functions.
   */
  def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit

  /**
   * Logs statement if enabled and condition is met with three field builder functions.
   */
  def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit

  /**
   * Logs statement if enabled and condition is met with four field builder functions.
   */
  def apply(
             condition: Condition,
             message: String,
             f1: FB => FieldBuilderResult,
             f2: FB => FieldBuilderResult,
             f3: FB => FieldBuilderResult,
             f4: FB => FieldBuilderResult
           )(implicit line: Line, enc: Enclosing, args: Args): Unit
}

object Logger {

  def apply[FB](core: CoreLogger, fieldBuilder: FB, sourceInfoMethod: (FB, Line, Enclosing, Args) => FieldBuilderResult): Logger[FB] = {
    new Impl[FB](core, fieldBuilder, sourceInfoMethod)
  }

  /**
   */
  class Impl[FB](val core: CoreLogger, val fieldBuilder: FB, sourceInfoMethod: (FB, Line, Enclosing, Args) => FieldBuilderResult) extends Logger[FB] {

    abstract class LoggerMethodBase(level: Level) extends EnabledLoggerMethod {
      override def enabled: Boolean = core.isEnabled(level)

      override def enabled(condition: Condition): Boolean = core.isEnabled(level, condition.asJava)
    }

    class DefaultDiagnosticLoggerMethod(level: Level) extends LoggerMethodBase(level) with DiagnosticLoggerMethod[FB] {

      import Implicits._

      private def sourceInfo(implicit line: Line, enc: Enclosing, args: Args): FieldBuilderResult = {
        sourceInfoMethod(fieldBuilder, line, enc, args)
      }

      override def apply(message: String)(implicit line: Line, enc: Enclosing, args: Args): Unit = {
        val f: FB => FieldBuilderResult = _ => sourceInfo
        core.log(level, message, f.asJava, fieldBuilder)
      }

      override def apply(message: String, f1: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ sourceInfo
        core.log(level, message, f.asJava, fieldBuilder)
      }

      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ sourceInfo
        core.log(level, message, f.asJava, fieldBuilder)
      }

      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ f3(fb) ++ sourceInfo
        core.log(level, message, f.asJava, fieldBuilder)
      }

      override def apply(
                          message: String,
                          f1: FB => FieldBuilderResult,
                          f2: FB => FieldBuilderResult,
                          f3: FB => FieldBuilderResult,
                          f4: FB => FieldBuilderResult
                        )(implicit line: Line, enc: Enclosing, args: Args): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ f3(fb) ++ f4(fb) ++ sourceInfo
        core.log(level, message, f.asJava, fieldBuilder)
      }

      override def apply(condition: Condition, message: String)(implicit line: Line, enc: Enclosing, args: Args): Unit = {
        val f: FB => FieldBuilderResult = _ => sourceInfo
        core.log(level, condition.asJava, message, f.asJava, fieldBuilder)
      }

      override def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ sourceInfo
        core.log(level, condition.asJava, message, f.asJava, fieldBuilder)
      }

      override def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ sourceInfo
        core.log(level, condition.asJava, message, f.asJava, fieldBuilder)
      }

      override def apply(
                          condition: Condition,
                          message: String,
                          f1: FB => FieldBuilderResult,
                          f2: FB => FieldBuilderResult,
                          f3: FB => FieldBuilderResult
                        )(implicit line: Line, enc: Enclosing, args: Args): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ f3(fb) ++ sourceInfo
        core.log(level, condition.asJava, message, f.asJava, fieldBuilder)
      }

      override def apply(
                          condition: Condition,
                          message: String,
                          f1: FB => FieldBuilderResult,
                          f2: FB => FieldBuilderResult,
                          f3: FB => FieldBuilderResult,
                          f4: FB => FieldBuilderResult
                        )(implicit line: Line, enc: Enclosing, args: Args): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ f3(fb) ++ f4(fb) ++ sourceInfo
        core.log(level, condition.asJava, message, f.asJava, fieldBuilder)
      }
    }

    class DefaultLoggerMethod(level: Level) extends LoggerMethodBase(level) with LoggerMethod[FB] {

      import Implicits._

      override def apply(message: String): Unit = core.log(level, message)

      override def apply(message: String, f1: FB => FieldBuilderResult): Unit = core.log(level, message, f1.asJava, fieldBuilder)

      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb)
        core.log(level, message, f.asJava, fieldBuilder)
      }

      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ f3(fb)
        core.log(level, message, f.asJava, fieldBuilder)
      }

      override def apply(
                          message: String,
                          f1: FB => FieldBuilderResult,
                          f2: FB => FieldBuilderResult,
                          f3: FB => FieldBuilderResult,
                          f4: FB => FieldBuilderResult
                        ): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ f3(fb) ++ f4(fb)
        core.log(level, message, f.asJava, fieldBuilder)
      }

      override def apply(condition: Condition, message: String): Unit = core.log(level, condition.asJava, message)

      override def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult): Unit =
        core.log(level, condition.asJava, message, f1.asJava, fieldBuilder)

      override def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb)
        core.log(level, condition.asJava, message, f.asJava, fieldBuilder)
      }

      override def apply(
                          condition: Condition,
                          message: String,
                          f1: FB => FieldBuilderResult,
                          f2: FB => FieldBuilderResult,
                          f3: FB => FieldBuilderResult
                        ): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ f3(fb)
        core.log(level, condition.asJava, message, f.asJava, fieldBuilder)
      }

      override def apply(
                          condition: Condition,
                          message: String,
                          f1: FB => FieldBuilderResult,
                          f2: FB => FieldBuilderResult,
                          f3: FB => FieldBuilderResult,
                          f4: FB => FieldBuilderResult
                        ): Unit = {
        val f: FB => FieldBuilderResult = fb => f1(fb) ++ f2(fb) ++ f3(fb) ++ f4(fb)
        core.log(level, condition.asJava, message, f.asJava, fieldBuilder)
      }
    }

    override val trace: DefaultDiagnosticLoggerMethod = new DefaultDiagnosticLoggerMethod(TRACE)

    override val debug: DefaultDiagnosticLoggerMethod = new DefaultDiagnosticLoggerMethod(DEBUG)

    override val info: DefaultLoggerMethod = new DefaultLoggerMethod(INFO)

    override val warn: DefaultLoggerMethod = new DefaultLoggerMethod(WARN)

    override val error: DefaultLoggerMethod = new DefaultLoggerMethod(ERROR)

    override def name: String = core.getName

    override def withCondition(condition: Condition): Logger[FB] = {
      condition match {
        case Condition.always =>
          this
        case Condition.never =>
          NoOp(core, fieldBuilder)
        case other =>
          newLogger(sourceInfoMethod, newCoreLogger = core.withCondition(other.asJava))
      }
    }

    override def withFields(f: FB => FieldBuilderResult): Logger[FB] = {
      newLogger(sourceInfoMethod, newCoreLogger = core.withFields(f.asJava, fieldBuilder))
    }

    override def withThreadContext: Logger[FB] = {
      newLogger(
        sourceInfoMethod,
        newCoreLogger = core.withThreadContext(Utilities.threadContext())
      )
    }

    override def withFieldBuilder[NEWFB](newFieldBuilder: NEWFB): Logger[NEWFB] = {
      throw new UnsupportedOperationException("Cannot create new field builder with source info method!")
    }

    @inline
    private def newLogger[T](
                              sourceInfoMethod: (T, Line, Enclosing, Args) => FieldBuilderResult,
                              newCoreLogger: CoreLogger = core,
                              newFieldBuilder: T = fieldBuilder
                            ): Logger[T] = new Impl[T](newCoreLogger, newFieldBuilder, sourceInfoMethod)
  }

  trait NoOp[FB] extends Logger[FB] {
    object NoOpLoggerMethod extends LoggerMethod[FB] {
      override def enabled: Boolean = false

      override def enabled(condition: Condition): Boolean = false

      override def apply(message: String): Unit = ()

      override def apply(message: String, f: FB => FieldBuilderResult): Unit = ()

      override def apply(condition: Condition, message: String): Unit = ()

      override def apply(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = ()

      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult): Unit = ()

      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult): Unit = ()

      override def apply(
                          message: String,
                          f1: FB => FieldBuilderResult,
                          f2: FB => FieldBuilderResult,
                          f3: FB => FieldBuilderResult,
                          f4: FB => FieldBuilderResult
                        ): Unit = ()

      override def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult): Unit = ()

      override def apply(
                          condition: Condition,
                          message: String,
                          f1: FB => FieldBuilderResult,
                          f2: FB => FieldBuilderResult,
                          f3: FB => FieldBuilderResult
                        ): Unit = ()

      override def apply(
                          condition: Condition,
                          message: String,
                          f1: FB => FieldBuilderResult,
                          f2: FB => FieldBuilderResult,
                          f3: FB => FieldBuilderResult,
                          f4: FB => FieldBuilderResult
                        ): Unit = ()
    }

    object NoOpDiagnosticMethod extends DiagnosticLoggerMethod[FB] {
      override def enabled: Boolean = false

      override def enabled(condition: Condition): Boolean = false

      /**
       * Logs statement if enabled.
       *
       * @param message
       * the message.
       */
      override def apply(message: String)(implicit line: Line, enc: Enclosing, args: Args): Unit = ()

      /**
       * Logs statement if enabled using a field builder function.
       */
      override def apply(message: String, f1: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = ()

      /**
       * Logs statement if enabled using two field builder functions.
       */
      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = ()

      /**
       * Logs statement if enabled using three field builder functions.
       */
      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = ()

      /**
       * Logs statement if enabled using four field builder functions.
       */
      override def apply(message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult, f4: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = ()

      /**
       * Logs statement if enabled and condition is met.
       */
      override def apply(condition: Condition, message: String)(implicit line: Line, enc: Enclosing, args: Args): Unit = ()

      /**
       * Logs statement if enabled and condition is met with a field builder function.
       */
      override def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = ()

      /**
       * Logs statement if enabled and condition is met with two field builder functions.
       */
      override def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = ()

      /**
       * Logs statement if enabled and condition is met with three field builder functions.
       */
      override def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = ()

      /**
       * Logs statement if enabled and condition is met with four field builder functions.
       */
      override def apply(condition: Condition, message: String, f1: FB => FieldBuilderResult, f2: FB => FieldBuilderResult, f3: FB => FieldBuilderResult, f4: FB => FieldBuilderResult)(implicit line: Line, enc: Enclosing, args: Args): Unit = ()
    }
  }

  object NoOp {
    def apply[FB](c: CoreLogger, fb: FB): NoOp[FB] = new NoOp[FB] {
      override def name: String = c.getName

      override def core: CoreLogger = c

      override def fieldBuilder: FB = fb

      override def withCondition(scalaCondition: Condition): Logger[FB] = this

      override def withFields(f: FB => FieldBuilderResult): Logger[FB] = this

      override def withThreadContext: Logger[FB] = this

      override def withFieldBuilder[T <: FB](newBuilder: T): Logger[T] = NoOp(core, newBuilder)

      override def trace: DiagnosticLoggerMethod[FB] = NoOpDiagnosticMethod

      override def debug: DiagnosticLoggerMethod[FB] = NoOpDiagnosticMethod

      override def info: LoggerMethod[FB] = NoOpLoggerMethod

      override def warn: LoggerMethod[FB] = NoOpLoggerMethod

      override def error: LoggerMethod[FB] = NoOpLoggerMethod
    }
  }
}
