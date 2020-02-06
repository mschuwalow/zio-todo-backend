package com.schuwalow.todo.repository

import cats.effect.Blocker
import cats.implicits._
import doobie._
import doobie.free.connection
import doobie.hikari._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import zio.macros.delegate._

import com.schuwalow.todo.config.DBConfig
import com.schuwalow.todo.{
  TodoId,
  TodoItem,
  TodoItemPatchForm,
  TodoItemPostForm,
  TodoPayload
}

final class DoobieTodoRepository(xa: Transactor[Task]) extends TodoRepository {
  import DoobieTodoRepository.SQL

  val todoRepository = new TodoRepository.Service[Any] {

    def getAll(): ZIO[Any, Nothing, List[TodoItem]] =
      SQL.getAll
        .to[List]
        .transact(xa)
        .orDie

    def getById(id: TodoId): ZIO[Any, Nothing, Option[TodoItem]] =
      SQL
        .get(id)
        .option
        .transact(xa)
        .orDie

    def delete(id: TodoId): ZIO[Any, Nothing, Unit] =
      SQL
        .delete(id)
        .run
        .transact(xa)
        .unit
        .orDie

    def deleteAll: ZIO[Any, Nothing, Unit] =
      SQL.deleteAll.run
        .transact(xa)
        .unit
        .orDie

    def create(todoItemForm: TodoItemPostForm): ZIO[Any, Nothing, TodoItem] =
      SQL
        .create(todoItemForm.asTodoPayload)
        .withUniqueGeneratedKeys[Long]("ID")
        .map(id => todoItemForm.asTodoItem(TodoId(id)))
        .transact(xa)
        .orDie

    def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
    ): ZIO[Any, Nothing, Option[TodoItem]] =
      (for {
        oldItem <- SQL.get(id).option
        newItem = oldItem.map(_.update(todoItemForm))
        _       <- newItem.fold(connection.unit)(item => SQL.update(item).run.void)
      } yield newItem)
        .transact(xa)
        .orDie
  }
}

object DoobieTodoRepository {

  def withDoobieTodoRepository(cfg: DBConfig) = {
    val initDb: Task[Unit] =
      Task {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
          .migrate()
      }.unit

    val mkTransactor: ZManaged[Blocking, Throwable, HikariTransactor[Task]] =
      ZIO.runtime[Blocking].toManaged_.flatMap { implicit rt =>
        for {
          transactEC <- rt.Environment.blocking.blockingExecutor
                         .map(_.asEC)
                         .toManaged_
          connectEC = rt.Platform.executor.asEC
          transactor <- HikariTransactor
                         .newHikariTransactor[Task](
                           cfg.driver,
                           cfg.url,
                           cfg.user,
                           cfg.password,
                           connectEC,
                           Blocker.liftExecutionContext(transactEC)
                         )
                         .toManaged
        } yield transactor
      }

    enrichWithManaged[TodoRepository] {
      initDb.toManaged_ *> mkTransactor.map(new DoobieTodoRepository(_))
    }
  }

  object SQL {

    def create(todo: TodoPayload): Update0 = sql"""
      INSERT INTO TODOS (TITLE, COMPLETED, ORDERING)
      VALUES (${todo.title}, ${todo.completed}, ${todo.order})
      """.update

    def get(id: TodoId): Query0[TodoItem] = sql"""
      SELECT * FROM TODOS WHERE ID = ${id.value}
      """.query[TodoItem]

    val getAll: Query0[TodoItem] = sql"""
      SELECT * FROM TODOS
      """.query[TodoItem]

    def delete(id: TodoId): Update0 = sql"""
      DELETE from TODOS WHERE ID = ${id.value}
      """.update

    val deleteAll: Update0 = sql"""
      DELETE from TODOS
      """.update

    def update(todoItem: TodoItem): Update0 = sql"""
      UPDATE TODOS SET
      TITLE = ${todoItem.item.title},
      COMPLETED = ${todoItem.item.completed},
      ORDERING = ${todoItem.item.order}
      WHERE ID = ${todoItem.id.value}
      """.update
  }

}
