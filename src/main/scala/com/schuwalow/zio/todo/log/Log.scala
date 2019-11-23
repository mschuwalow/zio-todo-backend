package com.schuwalow.zio.todo.log

import cats.Show
import zio.ZIO

trait Log extends Serializable {
  def log: Log.Service[Any]
}

object Log {

  trait UnsafeLogger {
    def withContext(ctx: String): UnsafeLogger

    def trace[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): Unit

    def debug[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): Unit

    def info[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): Unit

    def warn[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): Unit

    def error[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): Unit
  }

  trait Service[R] extends Serializable {

    def trace[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): ZIO[R, Nothing, Unit]

    def debug[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): ZIO[R, Nothing, Unit]

    def info[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): ZIO[R, Nothing, Unit]

    def warn[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): ZIO[R, Nothing, Unit]

    def error[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): ZIO[R, Nothing, Unit]

    val unsafeInstance: ZIO[R, Nothing, UnsafeLogger]
  }
}
