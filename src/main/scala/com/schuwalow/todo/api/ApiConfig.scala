package com.schuwalow.todo.api

import zio._
import zio.config.magnolia.deriveConfig

final case class ApiConfig(port: Int, baseUrl: String)

object ApiConfig {
  val desc: Config[ApiConfig] = deriveConfig[ApiConfig]
  val load: Task[ApiConfig]   = ZIO.configProviderWith(_.nested("api").nested("todo").load(desc))
}
