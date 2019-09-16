package com.schuwalow.zio.todo.http

import com.schuwalow.zio.todo._
import com.schuwalow.zio.todo.repository.TodoRepository
import com.schuwalow.zio.todo.repository.TodoRepository.InMemoryTodoRepository
import io.circe.Decoder
import io.circe.literal._
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.{ Status, _ }
import zio._
import zio.interop.catz._
import com.schuwalow.zio.todo.http.TodoService.TodoItemWithUri

class TodoServiceSpec extends HTTPSpec {
  import TodoServiceSpec._

  val app = todoService.orNotFound

  describe("TodoService") {

    it("should create new todo items") {
      runWithEnv {
        val req = request[TodoTask](Method.POST, "/").withEntity(json"""{"title": "Test"}""")
        check(
          app.run(req),
          Status.Created,
          Some(json"""{
          "id": 1,
          "url": "/1",
          "title": "Test",
          "completed":false,
          "order":null
        }""")
        )
      }
    }

    it("should list all todo items") {
      runWithEnv {
        val setupReq = request[TodoTask](Method.POST, "/").withEntity(json"""{"title": "Test"}""")
        val req      = request[TodoTask](Method.GET, "/")
        check(
          app.run(setupReq) *> app.run(setupReq) *> app.run(req),
          Status.Ok,
          Some(json"""[
            {"id": 1, "url": "/1", "title": "Test", "completed":false, "order":null},
            {"id": 2, "url": "/2", "title": "Test", "completed":false, "order":null}
          ]""")
        )
      }
    }

    it("should delete todo items by id") {
      runWithEnv {
        val setupReq  = request[TodoTask](Method.POST, "/").withEntity(json"""{"title": "Test"}""")
        val deleteReq = (id: Long) => request[TodoTask](Method.DELETE, s"/$id")
        val req       = request[TodoTask](Method.GET, "/")
        check(
          app
            .run(setupReq)
            .flatMap(resp => {
              implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[TodoTask, A] =
                jsonOf[TodoTask, A]
              resp.as[TodoItemWithUri].map(_.id)
            })
            .flatMap(id => app.run(deleteReq(id))) *> app.run(req),
          Status.Ok,
          Some(json"""[]""")
        )
      }
    }

    it("should delete all todo items") {
      runWithEnv {
        val setupReq  = request[TodoTask](Method.POST, "/").withEntity(json"""{"title": "Test"}""")
        val deleteReq = request[TodoTask](Method.DELETE, "/")
        val req       = request[TodoTask](Method.GET, "/")
        check(
          app.run(setupReq) *> app.run(setupReq) *> app.run(deleteReq) *> app.run(req),
          Status.Ok,
          Some(json"""[]""")
        )
      }
    }

    it("should update todo items") {
      runWithEnv {
        val setupReq = request[TodoTask](Method.POST, "/").withEntity(json"""{"title": "Test"}""")
        val updateReq =
          (id: Long) => request[TodoTask](Method.PATCH, s"/$id").withEntity(json"""{"title": "Test1"}""")
        val req = request[TodoTask](Method.GET, "/")
        check(
          app
            .run(setupReq)
            .flatMap(resp => {
              implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[TodoTask, A] =
                jsonOf[TodoTask, A]
              resp.as[TodoItemWithUri].map(_.id)
            })
            .flatMap(id => app.run(updateReq(id))) *> app.run(req),
          Status.Ok,
          Some(json"""[
            {"id": 1, "url": "/1", "title": "Test1", "completed":false, "order":null}
          ]""")
        )
      }
    }
  }
}

object TodoServiceSpec extends DefaultRuntime {
  type TodoTask[A] = TaskR[TodoRepository, A]

  val todoService = TodoService.routes[TodoRepository]("")

  val mkEnv: UIO[TodoRepository] =
    for {
      store   <- Ref.make(Map[TodoId, TodoItem]())
      counter <- Ref.make(0L)
      repo    = new InMemoryTodoRepository(store, counter)
      env = new TodoRepository {
        override val todoRepository: TodoRepository.Service[Any] = repo
      }
    } yield env

  def runWithEnv[E, A](task: ZIO[TodoRepository, E, A]): A =
    unsafeRun[E, A](mkEnv.flatMap(env => task.provide(env)))

  // implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[TodoTask, A] = jsonOf[TodoTask, A]
  // implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[TodoTask, A] = jsonEncoderOf[TodoTask, A]

}
