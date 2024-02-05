package com.schuwalow.todo

import doobie.Transactor
import doobie.hikari.{Config => HikariConfig, _}
import org.flywaydb.core.Flyway
import zio._
import zio.config.magnolia.deriveConfig
import zio.interop.catz._

object db {

  final case class DatabaseConfig(url: String, driver: String, user: String, password: String, cleanAllowed: Boolean)

  object DatabaseConfig {
    val desc: Config[DatabaseConfig] = deriveConfig[DatabaseConfig]
    val load: Task[DatabaseConfig]   = ZIO.configProviderWith(_.nested("db").nested("todo").load(desc))
  }

  def transactor: TaskLayer[Transactor[Task] with Flyway] =
    ZLayer.scopedEnvironment {
      def loadFlyway(cfg: DatabaseConfig): Task[Flyway] =
        ZIO.attempt {
          Flyway
            .configure()
            .dataSource(cfg.url, cfg.user, cfg.password)
            .cleanDisabled(!cfg.cleanAllowed)
            .load()
        }

      def makeTransactor(cfg: DatabaseConfig): RIO[Scope, HikariTransactor[Task]] = {
        val hikariConfig = HikariConfig(
          jdbcUrl = cfg.url,
          driverClassName = Some(cfg.driver),
          username = Some(cfg.user),
          password = Some(cfg.password)
        )
        HikariTransactor.fromConfig[Task](hikariConfig).toScopedZIO
      }

      for {
        cfg        <- DatabaseConfig.load
        flyway     <- loadFlyway(cfg)
        transactor <- makeTransactor(cfg)
      } yield ZEnvironment[Flyway, Transactor[Task]](flyway, transactor)
    }

  def migrated: TaskLayer[Transactor[Task] with Flyway] =
    transactor.tap(env => ZIO.attempt(env.get[Flyway].migrate()))

}
