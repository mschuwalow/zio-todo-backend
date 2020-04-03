package com.schuwalow.todo

import cats.Show
import zio._

package object log {
  type Log = Has[Log.Service]

  def trace[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM[Log](_.get.trace(a))

  def debug[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM[Log](_.get.debug(a))

  def info[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM[Log](_.get.info(a))

  def warn[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM[Log](_.get.warn(a))

  def error[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM[Log](_.get.error(a))

  val unsafeInstance =
    ZIO.accessM[Log](_.get.unsafeInstance)
}
