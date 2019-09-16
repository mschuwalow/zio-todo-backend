package com.schuwalow.zio.todo.http

import com.schuwalow.zio.todo._
import com.schuwalow.zio.todo.http.TodoService.TodoItemWithUri
import com.schuwalow.zio.todo.repository.TodoRepository
import com.schuwalow.zio.todo.repository.TodoRepository.InMemoryTodoRepository
import io.circe.generic.auto._
import org.http4s.implicits._
import org.http4s.{ Status, _ }
import zio._
import zio.interop.catz._

class TodoServiceSpec extends HTTPSpec {
  import TodoServiceSpec._
  import TodoServiceSpec.todoService._

  val app = todoService.service.orNotFound

  describe("TodoService") {

    it("should create new todo items") {
      val req = request(Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      runWithEnv(check(app.run(req), Status.Created, Some(TodoItemWithUri(1L, "/1", "Test", false, None))))
    }

    it("should list all todo items") {
      val setupReq = request[TodoTask](Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      val req      = request[TodoTask](Method.GET, "/")
      runWithEnv(
        check[TodoTask, List[TodoItemWithUri]](
          app.run(setupReq) *> app.run(setupReq) *> app.run(req),
          Status.Ok,
          Some(
            List(
              TodoItemWithUri(1L, "/1", "Test", false, None),
              TodoItemWithUri(2L, "/2", "Test", false, None)
            )
          )
        )
      )
    }

    it("should delete todo items by id") {
      val setupReq  = request[TodoTask](Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      val deleteReq = (id: Long) => request[TodoTask](Method.DELETE, s"/$id")
      val req       = request[TodoTask](Method.GET, "/")
      runWithEnv(
        check[TodoTask, List[TodoItemWithUri]](
          app
            .run(setupReq)
            .flatMap(resp => resp.as[TodoItemWithUri].map(_.id))
            .flatMap(id => app.run(deleteReq(id))) *> app.run(req),
          Status.Ok,
          Some(Nil)
        )
      )
    }

    it("should delete all todo items") {
      val setupReq  = request[TodoTask](Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      val deleteReq = request[TodoTask](Method.DELETE, "/")
      val req       = request[TodoTask](Method.GET, "/")
      runWithEnv(
        check[TodoTask, List[TodoItemWithUri]](
          app.run(setupReq) *> app.run(setupReq) *> app.run(deleteReq) *> app.run(req),
          Status.Ok,
          Some(Nil)
        )
      )
    }

    it("should update todo items") {
      val setupReq = request[TodoTask](Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      val updateReq =
        (id: Long) => request[TodoTask](Method.PATCH, s"/$id").withEntity(TodoItemPatchForm(title = Some("Test1")))
      val req = request[TodoTask](Method.GET, "/")
      runWithEnv(
        check[TodoTask, List[TodoItemWithUri]](
          app
            .run(setupReq)
            .flatMap(resp => resp.as[TodoItemWithUri].map(_.id))
            .flatMap(id => app.run(updateReq(id))) *> app.run(req),
          Status.Ok,
          Some(
            List(
              TodoItemWithUri(1L, "/1", "Test1", false, None)
            )
          )
        )
      )
    }
  }
}

object TodoServiceSpec extends DefaultRuntime {

  val todoService: TodoService[TodoRepository] = TodoService[TodoRepository]("")

  val mkEnv: UIO[TodoRepository] =
    for {
      store   <- Ref.make(Map[TodoId, TodoItem]())
      counter <- Ref.make(0L)
      repo    = InMemoryTodoRepository(store, counter)
      env = new TodoRepository {
        override val todoRepository: TodoRepository.Service[Any] = repo
      }
    } yield env

  def runWithEnv[E, A](task: ZIO[TodoRepository, E, A]): A =
    unsafeRun[E, A](mkEnv.flatMap(env => task.provide(env)))
}
