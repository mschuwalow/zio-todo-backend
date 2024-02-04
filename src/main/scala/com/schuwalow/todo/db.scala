package com.schuwalow.todo

import doobie.hikari.{Config => HikariConfig, _}
import org.flywaydb.core.Flyway
import zio._
import zio.config.magnolia.deriveConfig
import zio.interop.catz._
import doobie.Transactor

object db {

  final case class DatabaseConfig(url: String, driver: String, user: String, password: String)

  object DatabaseConfig {
    val desc: Config[DatabaseConfig] = deriveConfig[DatabaseConfig]
    val load: Task[DatabaseConfig]   = ZIO.configProviderWith(_.nested("todo").nested("db").load(desc))
  }

  def transactorLayer: TaskLayer[Transactor[Task]] = {
    def migrate(cfg: DatabaseConfig): Task[Unit] =
      ZIO.attempt {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
          .migrate()
      }.unit

    def mkTransactor(cfg: DatabaseConfig): RIO[Scope, HikariTransactor[Task]] = {
      val hikariConfig = HikariConfig(
        jdbcUrl = cfg.url,
        driverClassName = Some(cfg.driver),
        username = Some(cfg.user),
        password = Some(cfg.password)
      )
      HikariTransactor.fromConfig[Task](hikariConfig).toScopedZIO
    }

    ZLayer.scoped {
      for {
        cfg        <- DatabaseConfig.load
        _          <- migrate(cfg)
        transactor <- mkTransactor(cfg)
      } yield transactor
    }
  }
}
