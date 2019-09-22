package com.schuwalow.zio.todo

import cats.effect._
import com.schuwalow.zio.todo.config._
import com.schuwalow.zio.todo.http.TodoService
import com.schuwalow.zio.todo.log.Log
import com.schuwalow.zio.todo.log.Slf4jLogger.withSlf4jLogManaged
import com.schuwalow.zio.todo.repository._
import doobie.hikari._
import doobie.util.transactor.Transactor
import fs2.Stream.Compiler._
import org.flywaydb.core.Flyway
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.Router
import scala.concurrent.ExecutionContext
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.delegate.Mix
import zio.interop.catz._

object Main extends ManagedApp {

  type AppEnvironment = Clock with Console with Blocking with TodoRepository with Log
  type AppTask[A]     = TaskR[AppEnvironment, A]

  override def run(args: List[String]): ZManaged[Environment, Nothing, Int] =
    (for {
      cfg <- ZIO.fromEither(pureconfig.loadConfig[Config]).toManaged_
      _   <- initDb(cfg.dbConfig).toManaged_

      httpApp = Router[AppTask](
        "/todos" -> TodoService.routes(s"${cfg.appConfig.baseUrl}/todos")
      ).orNotFound

      _ <- ZManaged.environment[Environment] >>*
            withDoobieRepository(cfg.dbConfig) >>*
            withSlf4jLogManaged >>*
            runHttp(httpApp, cfg.appConfig.port).toManaged_

    } yield ())
      .foldM(err => putStrLn(s"Execution failed with: $err").const(1).toManaged_, _ => ZManaged.succeed(0))

  def withDoobieRepository[R <: Blocking](
    cfg: DBConfig
  )(implicit ev: R Mix TodoRepository): ZManaged[R, Throwable, R with TodoRepository] =
    for {
      blockingEC <- ZIO.environment[Blocking].flatMap(_.blocking.blockingExecutor.map(_.asEC)).toManaged_
      transactor <- mkTransactor(cfg, Platform.executor.asEC, blockingEC)
      r          <- DoobieTodoRepository.withDoobieTodoRepositoryManaged(transactor)
    } yield r

  def runHttp[R <: Clock](httpApp: HttpApp[TaskR[R, ?]], port: Int): ZIO[R, Throwable, Unit] = {
    type Task[A] = TaskR[R, A]
    ZIO.runtime[R].flatMap { implicit rts =>
      BlazeServerBuilder[Task]
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(CORS(httpApp))
        .serve
        .compile[Task, Task, ExitCode]
        .drain
    }
  }

  def initDb(cfg: DBConfig): Task[Unit] =
    ZIO.effect {
      val fw = Flyway
        .configure()
        .dataSource(cfg.url, cfg.user, cfg.password)
        .load()
      fw.migrate()
    }.unit

  def mkTransactor(
    cfg: DBConfig,
    connectEC: ExecutionContext,
    transactEC: ExecutionContext
  ): Managed[Throwable, Transactor[Task]] = {
    val xa =
      HikariTransactor.newHikariTransactor[Task](cfg.driver, cfg.url, cfg.user, cfg.password, connectEC, transactEC)
    ZIO.runtime[Any].toManaged_.flatMap { implicit rt =>
      xa.toManaged
    }
  }
}
