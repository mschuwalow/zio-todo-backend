package com.schuwalow.todo

import cats.effect._
import fs2.Stream.Compiler._
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import pureconfig.ConfigSource
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.interop.catz._
import zio.macros.delegate._

import com.schuwalow.todo.config._
import com.schuwalow.todo.http.TodoService
import com.schuwalow.todo.http.TodoService
import com.schuwalow.todo.log.Log
import com.schuwalow.todo.log.Log
import com.schuwalow.todo.log.Slf4jLogger.withSlf4jLogger
import com.schuwalow.todo.repository.DoobieTodoRepository.withDoobieTodoRepository
import com.schuwalow.todo.repository.TodoRepository

object Main extends ManagedApp {

  type AppEnvironment = Clock
    with Console
    with Blocking
    with TodoRepository
    with Log
  type AppTask[A] = RIO[AppEnvironment, A]

  override def run(args: List[String]): ZManaged[ZEnv, Nothing, Int] =
    (for {
      cfg <- ZIO.fromEither(ConfigSource.default.load[Config]).toManaged_

      httpApp = Router[AppTask](
        "/todos" -> TodoService.routes(s"${cfg.appConfig.baseUrl}/todos")
      ).orNotFound

      _ <- ZIO.environment[ZEnv] @@
            withSlf4jLogger @@
            withDoobieTodoRepository(cfg.dbConfig) >>>
            runHttp(httpApp, cfg.appConfig.port).toManaged_

    } yield ())
      .foldM(
        err => putStrLn(s"Execution failed with: $err").as(1).toManaged_,
        _ => ZManaged.succeed(0)
      )

  def runHttp[R <: Clock](
    httpApp: HttpApp[TaskR[R, ?]],
    port: Int
  ): ZIO[R, Throwable, Unit] = {
    type Task[A] = RIO[R, A]
    ZIO.runtime[R].flatMap { implicit rts =>
      BlazeServerBuilder[Task]
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(CORS(httpApp))
        .serve
        .compile[Task, Task, ExitCode]
        .drain
    }
  }
}
