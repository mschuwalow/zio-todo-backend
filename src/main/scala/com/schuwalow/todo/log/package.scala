package com.schuwalow.todo

import cats.Show
import zio._

package object log extends Log.Service[Log] {

  def trace[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM(_.log.trace(a))

  def debug[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM(_.log.debug(a))

  def info[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM(_.log.info(a))

  def warn[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM(_.log.warn(a))

  def error[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM(_.log.error(a))

  val unsafeInstance =
    ZIO.accessM(_.log.unsafeInstance)
}
