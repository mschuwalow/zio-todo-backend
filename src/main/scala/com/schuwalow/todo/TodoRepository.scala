package com.schuwalow.todo

import com.schuwalow.todo.{TodoId, TodoItem, TodoItemPatchForm, TodoItemPostForm}
import zio._

trait TodoRepository {
  def getAll: UIO[List[TodoItem]]

  def getById(id: TodoId): UIO[Option[TodoItem]]

  def delete(id: TodoId): UIO[Unit]

  def deleteAll: UIO[Unit]

  def create(todoItemForm: TodoItemPostForm): UIO[TodoItem]

  def update(id: TodoId, todoItemForm: TodoItemPatchForm): UIO[Option[TodoItem]]
}

object TodoRepository {

  def create(todoItemForm: TodoItemPostForm): URIO[TodoRepository, TodoItem] =
    ZIO.serviceWithZIO(_.create(todoItemForm))

  def getById(id: TodoId): URIO[TodoRepository, Option[TodoItem]] = ZIO.serviceWithZIO(_.getById(id))

  val getAll: URIO[TodoRepository, List[TodoItem]] = ZIO.serviceWithZIO(_.getAll)

  def delete(id: TodoId): URIO[TodoRepository, Unit] = ZIO.serviceWithZIO(_.delete(id))

  val deleteAll: URIO[TodoRepository, Unit] = ZIO.serviceWithZIO(_.deleteAll)

  def update(id: TodoId, todoItemForm: TodoItemPatchForm): URIO[TodoRepository, Option[TodoItem]] =
    ZIO.serviceWithZIO(_.update(id, todoItemForm))
}
