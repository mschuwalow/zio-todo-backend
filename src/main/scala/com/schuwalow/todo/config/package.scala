package com.schuwalow.todo

import zio._

package object config {
  type AppConfig      = Has[AppConfig.Config]
  type HttpConfig     = Has[HttpConfig.Config]
  type DatabaseConfig = Has[DatabaseConfig.Config]

  val getAppConfig: URIO[AppConfig, AppConfig.Config] =
    ZIO.access(_.get)

  val getHttpConfig: URIO[HttpConfig, HttpConfig.Config] =
    ZIO.access(_.get)

  val getDatabaseConfig: URIO[DatabaseConfig, DatabaseConfig.Config] =
    ZIO.access(_.get)
}
