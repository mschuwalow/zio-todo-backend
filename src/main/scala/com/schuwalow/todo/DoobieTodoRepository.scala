package com.schuwalow.todo

import cats.implicits._
import com.schuwalow.todo._
import doobie._
import doobie.free.connection
import doobie.implicits._
import doobie.util.transactor.Transactor
import zio._
import zio.interop.catz._

final private class DoobieTodoRepository(xa: Transactor[Task]) extends TodoRepository {
  import DoobieTodoRepository.SQL

  override def getAll: UIO[List[TodoItem]] =
    SQL.getAll
      .to[List]
      .transact(xa)
      .orDie

  override def getById(id: TodoId): UIO[Option[TodoItem]] =
    SQL
      .get(id)
      .option
      .transact(xa)
      .orDie

  override def delete(id: TodoId): UIO[Unit] =
    SQL
      .delete(id)
      .run
      .transact(xa)
      .unit
      .orDie

  override def deleteAll: UIO[Unit] =
    SQL.deleteAll.run
      .transact(xa)
      .unit
      .orDie

  override def create(todoItemForm: TodoItemPostForm): UIO[TodoItem] =
    SQL
      .create(todoItemForm.asTodoPayload)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => todoItemForm.asTodoItem(TodoId(id)))
      .transact(xa)
      .orDie

  override def update(
    id: TodoId,
    todoItemForm: TodoItemPatchForm
  ): UIO[Option[TodoItem]] =
    (for {
      oldItem <- SQL.get(id).option
      newItem  = oldItem.map(_.update(todoItemForm))
      _       <- newItem.fold(connection.unit)(item => SQL.update(item).run.void)
    } yield newItem)
      .transact(xa)
      .orDie
}

object DoobieTodoRepository {

  def layer: RLayer[Transactor[Task], TodoRepository] = ZLayer.fromFunction(new DoobieTodoRepository(_))

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
