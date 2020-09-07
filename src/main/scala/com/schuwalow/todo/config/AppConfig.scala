package com.schuwalow.todo.config

import pureconfig._
import pureconfig.generic.semiauto._
import zio._

object AppConfig {

  final case class Config(http: HttpConfig.Config, database: DatabaseConfig.Config)

  object Config {
    implicit val convert: ConfigConvert[Config] = deriveConvert
  }

  val live: ZLayer[Any, IllegalStateException, AppConfig] =
    ZLayer.fromEffect {
      ZIO
        .fromEither(ConfigSource.default.load[Config])
        .mapError(failures =>
          new IllegalStateException(
            s"Error loading configuration: $failures"
          )
        )
    }

}
