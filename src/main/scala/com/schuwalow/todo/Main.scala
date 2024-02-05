package com.schuwalow.todo

import zio._
import zio.config.typesafe.TypesafeConfigProvider
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  override val bootstrap =
    (Runtime.removeDefaultLoggers >>> SLF4J.slf4j) ++
      Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath().kebabCase)

  def run =
    ZLayer
      .make[Any](
        api.startApi,
        db.migrated,
        DoobieTodoRepository.layer
      )
      .launch
      .exitCode
}
