package com.schuwalow.zio.todo.http

import com.schuwalow.zio.todo.UnitSpec._
import com.schuwalow.zio.todo.repository.TodoRepository
import com.schuwalow.zio.todo.repository.TodoRepository.InMemoryTodoRepository
import com.schuwalow.zio.todo.{TodoItem, TodoItemPostForm, TodoItemWithUri, UnitSpec}
import io.circe.generic.auto._
import org.http4s.{Status, _}
import org.http4s.implicits._
import scalaz.zio._
import scalaz.zio.interop.catz._

class TodoServiceSpec extends UnitSpec with DefaultRuntime {

  import TodoServiceSpec._
  import TodoServiceSpec.todoService._

  val app = todoService.service.orNotFound

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

    it("should list todo items after creating") {
      val setupReq  = request[TodoTask](Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      val req       = request[TodoTask](Method.GET, "/")
      val result    = unsafeRun(mkEnv.flatMap { env =>
        check[TodoTask, List[TodoItemWithUri]](
          app.run(setupReq) *> app.run(setupReq) *> app.run(req),
          Status.Ok,
          Some(List(
            TodoItemWithUri("/1", 1L, "Test", false, None),
            TodoItemWithUri("/2", 2L, "Test", false, None)
          ))).provide(env)
      })
      assert(result)
    }

  }

}

object TodoServiceSpec {

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
