package com.schuwalow.todo.log

import cats.Show
import zio._

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

  trait Service extends Serializable {

    def trace[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): ZIO[Any, Nothing, Unit]

    def debug[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): ZIO[Any, Nothing, Unit]

    def info[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): ZIO[Any, Nothing, Unit]

    def warn[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): ZIO[Any, Nothing, Unit]

    def error[A: Show](
      a: => A
    )(implicit
      line: sourcecode.Line,
      file: sourcecode.File
    ): ZIO[Any, Nothing, Unit]

    val unsafeInstance: ZIO[Any, Nothing, UnsafeLogger]
  }
}
