package com.schuwalow.todo

import zio.ZLayer
import zio.blocking.Blocking
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

import com.schuwalow.todo.config._
import com.schuwalow.todo.repository._

object layers {

  type Layer0Env =
    AppConfig with Logging with Blocking

  type Layer1Env =
    Layer0Env with HttpConfig with DatabaseConfig

  type Layer2Env =
    Layer1Env with TodoRepository

  type AppEnv = Layer2Env

  object live {

    val layer0: ZLayer[Blocking, Throwable, Layer0Env] =
      Blocking.any ++ AppConfig.live ++ Slf4jLogger.make((_, msg) => msg)

    val layer1: ZLayer[Layer0Env, Throwable, Layer1Env] =
      HttpConfig.fromAppConfig ++ DatabaseConfig.fromAppConfig ++ ZLayer.identity

    val layer2: ZLayer[Layer1Env, Throwable, Layer2Env] =
      DoobieTodoRepository.layer ++ ZLayer.identity

    val appLayer: ZLayer[Blocking, Throwable, AppEnv] =
      layer0 >>> layer1 >>> layer2
  }
}
