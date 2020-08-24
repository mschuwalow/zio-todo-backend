package com.schuwalow.todo

import zio.ZLayer
import zio.blocking.Blocking
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

import com.schuwalow.todo.config.AppConfigProvider
import com.schuwalow.todo.config.ConfigProvider
import com.schuwalow.todo.config.DbConfigProvider
import com.schuwalow.todo.repository.DoobieTodoRepository
import com.schuwalow.todo.repository.TodoRepository

object layers {

  type Layer0Env =
    ConfigProvider with Logging with Blocking

  type Layer1Env =
    Layer0Env with AppConfigProvider with DbConfigProvider

  type Layer2Env =
    Layer1Env with TodoRepository

  type AppEnv = Layer2Env

  object live {

    val layer0: ZLayer[Blocking, Throwable, Layer0Env] =
      Blocking.any ++ ConfigProvider.live ++ Slf4jLogger.make((_, msg) => msg)

    val layer1: ZLayer[Layer0Env, Throwable, Layer1Env] =
      AppConfigProvider.fromConfig ++ DbConfigProvider.fromConfig ++ ZLayer.identity

    val layer2: ZLayer[Layer1Env, Throwable, Layer2Env] =
      DoobieTodoRepository.layer ++ ZLayer.identity

    val appLayer: ZLayer[Blocking, Throwable, AppEnv] =
      layer0 >>> layer1 >>> layer2
  }
}
