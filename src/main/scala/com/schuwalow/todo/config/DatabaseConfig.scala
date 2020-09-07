package com.schuwalow.todo.config

import pureconfig._
import pureconfig.generic.semiauto._
import zio._

object DatabaseConfig {

  final case class Config(url: String, driver: String, user: String, password: String)

  object Config {
    implicit val convert: ConfigConvert[Config] = deriveConvert
  }

  val fromAppConfig: ZLayer[AppConfig, Nothing, DatabaseConfig] =
    ZLayer.fromService(_.database)
}
