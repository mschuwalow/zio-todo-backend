package com.schuwalow.todo

import org.flywaydb.core.Flyway
import zio.ZIO
import zio.test.Assertion._
import zio.test._

object TodoRepositoySpec extends BaseSpec {
  def makeSuite(name: String) = suite(name)(
    test("create, getById") {
      for {
        todoItem <- TodoRepository.create(TodoItemPostForm("test"))
        result   <- TodoRepository.getById(todoItem.id)
      } yield assert(result)(isSome(equalTo(todoItem)))
    },
    test("getAll") {
      for {
        todoItem1 <- TodoRepository.create(TodoItemPostForm("test1"))
        todoItem2 <- TodoRepository.create(TodoItemPostForm("test2"))
        result    <- TodoRepository.getAll
      } yield assert(result)(equalTo(List(todoItem1, todoItem2)))
    },
    test("delete") {
      for {
        todoItem <- TodoRepository.create(TodoItemPostForm("test"))
        _        <- TodoRepository.delete(todoItem.id)
        result   <- TodoRepository.getById(todoItem.id)
      } yield assert(result)(isNone)
    },
    test("deleteAll") {
      for {
        _      <- TodoRepository.create(TodoItemPostForm("test"))
        _      <- TodoRepository.create(TodoItemPostForm("test"))
        _      <- TodoRepository.deleteAll
        result <- TodoRepository.getAll
      } yield assert(result)(isEmpty)
    },
    test("update") {
      for {
        todoItem <- TodoRepository.create(TodoItemPostForm("test"))
        _        <- TodoRepository.update(todoItem.id, TodoItemPatchForm(Some("updated")))
        result   <- TodoRepository.getById(todoItem.id)
      } yield assert(result)(isSome(hasField("title", _.item.title, equalTo("updated"))))
    }
  )

  val spec = suite("TodoRepository")(
    makeSuite("InMemoryTodoRepository").provide(InMemoryTodoRepository.layer),
    makeSuite("DoobieTodoRepository")
      .@@(
        TestAspect.around(
          ZIO.serviceWithZIO[Flyway](fw => ZIO.attempt(fw.migrate())),
          ZIO.serviceWithZIO[Flyway](fw => ZIO.attempt(fw.clean()).orDie)
        )
      )
      .provideShared(DoobieTodoRepository.layer, db.transactor)
  )

}
