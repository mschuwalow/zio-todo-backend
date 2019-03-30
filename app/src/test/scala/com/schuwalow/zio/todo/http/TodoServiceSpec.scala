package com.schuwalow.zio.todo.http

import com.schuwalow.zio.todo.UnitSpec._
import com.schuwalow.zio.todo.repository.TodoRepository
import com.schuwalow.zio.todo.repository.TodoRepository.InMemoryTodoRepository
import com.schuwalow.zio.todo._
import io.circe.generic.auto._
import org.http4s.implicits._
import org.http4s.{Status, _}
import org.scalatest.Assertion
import scalaz.zio._
import scalaz.zio.interop.catz._

class TodoServiceSpec extends UnitSpec {

  import TodoServiceSpec._
  import TodoServiceSpec.todoService._

  val app = todoService.service.orNotFound

  def verifyWithEnv[E](task: ZIO[TodoRepository, E, Boolean]): Assertion =
    unsafeRun[E, Assertion](mkEnv.flatMap(env => task.provide(env).map(x => assert(x))))

  describe("TodoService") {

    it("should create new todo items") {
      val req     = request(Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      val result  = unsafeRun(mkEnv.flatMap { env =>
        check[TodoTask, TodoItemWithUri](
          app.run(req),
          Status.Created,
          Some(TodoItemWithUri("/1", 1L, "Test", false, None))).provide(env)
      })
      assert(result)
    }

    it("should list all todo items") {
      val setupReq  = request[TodoTask](Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      val req       = request[TodoTask](Method.GET, "/")
      verifyWithEnv(
        check[TodoTask, List[TodoItemWithUri]](
          app.run(setupReq) *> app.run(setupReq) *> app.run(req),
          Status.Ok,
          Some(List(
            TodoItemWithUri("/1", 1L, "Test", false, None),
            TodoItemWithUri("/2", 2L, "Test", false, None)
          ))))
    }

    it("should delete todo items by id") {
      val setupReq  = request[TodoTask](Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      val deleteReq = (id: Long) => request[TodoTask](Method.DELETE, s"/$id")
      val req       = request[TodoTask](Method.GET, "/")
      verifyWithEnv(
        check[TodoTask, List[TodoItemWithUri]](
          app.run(setupReq).flatMap(resp => resp.as[TodoItemWithUri].map(_.id)).flatMap(id => app.run(deleteReq(id))) *> app.run(req),
          Status.Ok,
          Some(Nil)))
    }

    it("should delete all todo items") {
      val setupReq  = request[TodoTask](Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      val deleteReq = request[TodoTask](Method.DELETE, "/")
      val req       = request[TodoTask](Method.GET, "/")
      verifyWithEnv(
        check[TodoTask, List[TodoItemWithUri]](
          app.run(setupReq) *> app.run(setupReq) *> app.run(deleteReq) *> app.run(req),
          Status.Ok,
          Some(Nil)))
    }

    it("should update todo items") {
      val setupReq  = request[TodoTask](Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      val updateReq = (id: Long) => request[TodoTask](Method.PATCH, s"/$id").withEntity(TodoItemPatchForm(title = Some("Test1")))
      val req       = request[TodoTask](Method.GET, "/")
      verifyWithEnv(
        check[TodoTask, List[TodoItemWithUri]](
          app.run(setupReq).flatMap(resp => resp.as[TodoItemWithUri].map(_.id)).flatMap(id => app.run(updateReq(id))) *> app.run(req),
          Status.Ok,
          Some(List(
            TodoItemWithUri("/1", 1L, "Test1", false, None)
          ))))
    }
  }
}

object TodoServiceSpec extends DefaultRuntime {

  val todoService: TodoService[TodoRepository] = TodoService[TodoRepository]("")

  val mkEnv: UIO[TodoRepository] =
    for {
      store    <- Ref.make(Map[Long, TodoItem]())
      counter  <- Ref.make(0L)
      repo      = InMemoryTodoRepository(store, counter)
      env       = new TodoRepository {
                    override val todoRepository: TodoRepository.Service[Any] = repo
                  }
    } yield env

}
