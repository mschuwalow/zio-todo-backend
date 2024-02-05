package com.schuwalow.todo

import com.typesafe.config.ConfigFactory
import zio._
import zio.config.typesafe.TypesafeConfigProvider
import zio.test._

trait BaseSpec extends ZIOSpecDefault {
  override val bootstrap =
    Runtime.removeDefaultLoggers ++
      Runtime.setConfigProvider(
        TypesafeConfigProvider
          .fromTypesafeConfig(ConfigFactory.load("application.test.conf").resolve)
          .kebabCase
      ) ++
      testEnvironment
}
