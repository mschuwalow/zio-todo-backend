package com.schuwalow.todo

import pureconfig.ConfigConvert
import pureconfig.ConfigSource
import pureconfig.generic.semiauto._
import zio.Has
import zio.ZIO
import zio.ZLayer

object config {

  final case class Config(appConfig: AppConfig, dbConfig: DBConfig)

  object Config {
    implicit val convert: ConfigConvert[Config] = deriveConvert
  }

  type ConfigProvider = Has[Config]

  object ConfigProvider {

    val live: ZLayer[Any, IllegalStateException, Has[Config]] =
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

  type DbConfigProvider = Has[DBConfig]

  object DbConfigProvider {

    val fromConfig: ZLayer[ConfigProvider, Nothing, DbConfigProvider] =
      ZLayer.fromService(_.dbConfig)
  }

  type AppConfigProvider = Has[AppConfig]

  object AppConfigProvider {

    val fromConfig: ZLayer[ConfigProvider, Nothing, AppConfigProvider] =
      ZLayer.fromService(_.appConfig)
  }

  final case class AppConfig(port: Int, baseUrl: String)

  object AppConfig {
    implicit val convert: ConfigConvert[AppConfig] = deriveConvert
  }

  final case class DBConfig(url: String, driver: String, user: String, password: String)

  object DBConfig {
    implicit val convert: ConfigConvert[DBConfig] = deriveConvert
  }
}
