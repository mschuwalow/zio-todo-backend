package com.schuwalow.todo.log

import Log.UnsafeLogger
import cats.Show
import org.slf4j
import org.slf4j.{ Logger => SLogger }
import zio._

trait Slf4jLogger {
  val log = new Slf4jLogger.ServiceImpl(Slf4jLogger.unsafeInstance)
}

object Slf4jLogger {

  object Global extends Slf4jLogger

  val unsafeInstance: UnsafeLogger =
    new UnsafeLoggerImpl(
      slf4j.LoggerFactory.getLogger("ROOT"),
      _.value.split("/").last
    )

  def fromSlf4j(
    inner: SLogger,
    shorten: sourcecode.File => String
  ): Log.Service =
    new ServiceImpl(new UnsafeLoggerImpl(inner, shorten))

  val layer: ZLayer[Any, Nothing, Log] = ZLayer.succeed(Global.log)

  final private[Slf4jLogger] class UnsafeLoggerImpl(
    private[this] val inner: SLogger,
    private[this] val showFile: sourcecode.File => String,
    private[this] val context: String = "<?>")
      extends UnsafeLogger { self =>

    def withContext(ctx: String): UnsafeLoggerImpl =
      new UnsafeLoggerImpl(
        inner,
        showFile,
        ctx
      )

    def trace[A](
      a: => A
    )(implicit
      S: Show[A],
      line: sourcecode.Line,
      file: sourcecode.File
    ) =
      if (inner.isTraceEnabled()) {
        inner.trace(
          s"${showFile(file)}:${line.value} - $context - ${S.show(a)}"
        )
      }

    def debug[A](
      a: => A
    )(implicit
      S: Show[A],
      line: sourcecode.Line,
      file: sourcecode.File
    ) =
      if (inner.isDebugEnabled()) {
        inner.debug(
          s"${showFile(file)}:${line.value} - $context - ${S.show(a)}"
        )
      }

    def info[A](
      a: => A
    )(implicit
      S: Show[A],
      line: sourcecode.Line,
      file: sourcecode.File
    ) =
      if (inner.isInfoEnabled()) {
        inner.info(s"${showFile(file)}:${line.value} - $context - ${S.show(a)}")
      }

    def warn[A](
      a: => A
    )(implicit
      S: Show[A],
      line: sourcecode.Line,
      file: sourcecode.File
    ) =
      if (inner.isWarnEnabled()) {
        inner.warn(s"${showFile(file)}:${line.value} - $context - ${S.show(a)}")
      }

    def error[A](
      a: => A
    )(implicit
      S: Show[A],
      line: sourcecode.Line,
      file: sourcecode.File
    ) =
      if (inner.isErrorEnabled()) {
        inner.error(
          s"${showFile(file)}:${line.value} - $context - ${S.show(a)}"
        )
      }
  }

  final private[Slf4jLogger] class ServiceImpl(
    private[this] val inner: UnsafeLogger)
      extends Log.Service {

    def trace[A](
      a: => A
    )(implicit
      S: Show[A],
      line: sourcecode.Line,
      file: sourcecode.File
    ) =
      withFiberContext(_.trace(a))

    def debug[A](
      a: => A
    )(implicit
      S: Show[A],
      line: sourcecode.Line,
      file: sourcecode.File
    ) =
      withFiberContext(_.debug(a))

    def info[A](
      a: => A
    )(implicit
      S: Show[A],
      line: sourcecode.Line,
      file: sourcecode.File
    ) =
      withFiberContext(_.info(a))

    def warn[A](
      a: => A
    )(implicit
      S: Show[A],
      line: sourcecode.Line,
      file: sourcecode.File
    ) =
      withFiberContext(_.warn(a))

    def error[A](
      a: => A
    )(implicit
      S: Show[A],
      line: sourcecode.Line,
      file: sourcecode.File
    ) =
      withFiberContext(_.error(a))

    def withFiberContext[A](f: UnsafeLogger => A): ZIO[Any, Nothing, A] =
      ZIO.descriptorWith { desc =>
        ZIO.effectTotal(f(inner.withContext(s"<${desc.id}>")))
      }

    val unsafeInstance =
      withFiberContext(identity)
  }

}
