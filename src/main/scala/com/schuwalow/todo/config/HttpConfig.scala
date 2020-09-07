package com.schuwalow.todo.config

import pureconfig._
import pureconfig.generic.semiauto._
import zio._

object HttpConfig {

  final case class Config(port: Int, baseUrl: String)

  object Config {
    implicit val convert: ConfigConvert[Config] = deriveConvert
  }

  val fromAppConfig: ZLayer[AppConfig, Nothing, HttpConfig] =
    ZLayer.fromService(_.http)
}
