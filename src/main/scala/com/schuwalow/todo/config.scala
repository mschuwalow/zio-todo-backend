package com.schuwalow.todo

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto._

object config {

  final case class Config(
    appConfig: AppConfig,
    dbConfig: DBConfig)

  object Config {
    implicit val convert: ConfigConvert[Config] = deriveConvert
  }

  final case class AppConfig(
    port: Int,
    baseUrl: String)

  object AppConfig {
    implicit val convert: ConfigConvert[AppConfig] = deriveConvert
  }

  final case class DBConfig(
    url: String,
    driver: String,
    user: String,
    password: String)

  object DBConfig {
    implicit val convert: ConfigConvert[DBConfig] = deriveConvert
  }
}
