package com.schuwalow.todo

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import zio._
import zio.interop.catz._

package object api {
  type Env = TodoRoutes.Env

  private type AppTask[A] = RIO[Env, A]

  val startApi = ZLayer.scoped {
    for {
      cfg <- ApiConfig.load

      todos  = TodoRoutes.routes[Env](s"{cfg.baseUrl}/todos")
      router = Router("/todos" -> todos).orNotFound
      app    = CORS.policy(router)

      server = BlazeServerBuilder[AppTask]
                 .bindHttp(cfg.port, "0.0.0.0")
                 .withHttpApp(app)
                 .serve
                 .compile
                 .drain

      _ <- server.forkScoped

    } yield ()
  }

}
