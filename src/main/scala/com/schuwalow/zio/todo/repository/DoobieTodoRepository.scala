package com.schuwalow.zio.todo.repository
import com.schuwalow.zio.todo._
import com.schuwalow.zio.todo.repository.DoobieTodoRepository.SQL
import doobie._
import doobie.implicits._
import zio.interop.catz._
import cats.effect.Blocker
import zio._
import zio.macros.delegate._
import cats.implicits._
import doobie.free.connection
import doobie.util.transactor.Transactor
import zio.blocking.Blocking
import com.schuwalow.zio.todo.config._
import doobie.hikari._
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import scala.concurrent.ExecutionContext
import zio._
import zio.blocking.Blocking
import zio.interop.catz._

final class DoobieTodoRepository(xa: Transactor[Task]) extends TodoRepository {

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
    val initDb: Task[Unit] = Task {
      Flyway
        .configure()
        .dataSource(cfg.url, cfg.user, cfg.password)
        .load()
        .migrate()
    }.unit

    def mkTransactor(blockingEC: ExecutionContext) =
      ZIO.runtime[Any].toManaged_.flatMap { implicit rt =>
        HikariTransactor
          .newHikariTransactor[Task](
            cfg.driver,
            cfg.url,
            cfg.user,
            cfg.password,
            rt.Platform.executor.asEC,
            Blocker.liftExecutionContext(blockingEC)
          )
          .toManaged
      }

    enrichWithManaged[TodoRepository] {
      for {
        _ <- initDb.toManaged_
        blockingEC <- ZIO
                       .accessM[Blocking](
                         _.blocking.blockingExecutor.map(_.asEC)
                       )
                       .toManaged_
        transactor <- mkTransactor(blockingEC)
      } yield new DoobieTodoRepository(transactor)
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
