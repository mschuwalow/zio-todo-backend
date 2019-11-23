package com.schuwalow.zio.todo

import cats.Show
import zio._

package object log extends Log.Service[Log] {

  final def trace[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM(_.log.trace(a))

  final def debug[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM(_.log.debug(a))

  final def info[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM(_.log.info(a))

  final def warn[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM(_.log.warn(a))

  final def error[A: Show](
    a: => A
  )(implicit
    line: sourcecode.Line,
    file: sourcecode.File
  ) =
    ZIO.accessM(_.log.error(a))

  final def unsafeInstance =
    ZIO.accessM(_.log.unsafeInstance)
}
