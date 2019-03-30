package com.schuwalow.zio.todo

import cats.effect._
import com.schuwalow.zio.todo.http.TodoService
import com.schuwalow.zio.todo.repository.TodoRepository.InMemoryTodoRepository
import com.schuwalow.zio.todo.repository._
import fs2.Stream.Compiler._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import pureconfig.generic.auto._
import scalaz.zio._
import scalaz.zio.clock.Clock
import scalaz.zio.console._
import scalaz.zio.interop.catz._
import scalaz.zio.scheduler.Scheduler

object Main extends App {

  type AppEnvironment = Clock with Console with TodoRepository
  type AppTask[A] = TaskR[AppEnvironment, A]

  final case class Port(value: Int) extends AnyVal
  final case class BaseUrl(value: String) extends AnyVal

  final case class Config(
    port: Port,
    baseUrl: BaseUrl
  )

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    (for {
      config   <- ZIO.fromEither(pureconfig.loadConfig[Config])
      httpApp   = Router[AppTask](
            "/todos" -> TodoService(s"${config.baseUrl.value}/todos").service
                  ).orNotFound
      server    = ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
                    BlazeServerBuilder[AppTask]
                      .bindHttp(config.port.value)
                      .withHttpApp(CORS(httpApp))
                      .serve
                      .compile[AppTask, AppTask, ExitCode]
                      .drain
                  }
      store    <- Ref.make(Map[Long, TodoItem]())
      counter  <- Ref.make(0L)
      repo      = InMemoryTodoRepository(store, counter)
      program  <- server.provideSome[Environment] { base =>
        new Clock with Console with TodoRepository {
          override val clock: Clock.Service[Any]                    = base.clock
          override val console: Console.Service[Any]                = base.console
          override val scheduler: Scheduler.Service[Any]            = base.scheduler
          override val todoRepository: TodoRepository.Service[Any]  = repo
        }
      }
    } yield program).foldM(err => putStrLn(s"Execution failed with: $err") *> ZIO.succeed(1), _ => ZIO.succeed(0))

}
