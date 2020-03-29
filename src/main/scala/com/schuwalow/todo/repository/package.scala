package com.schuwalow.todo

import zio._

package object repository {
  type TodoRepository = Has[TodoRepository.Service]

  def create(
    todoItemForm: TodoItemPostForm
  ): ZIO[TodoRepository, Nothing, TodoItem] =
    ZIO.accessM(_.get.create(todoItemForm))

  def getById(id: TodoId): ZIO[TodoRepository, Nothing, Option[TodoItem]] =
    ZIO.accessM(_.get.getById(id))

  def getAll: ZIO[TodoRepository, Nothing, List[TodoItem]] =
    ZIO.accessM(_.get.getAll)

  def delete(id: TodoId): ZIO[TodoRepository, Nothing, Unit] =
    ZIO.accessM(_.get.delete(id))

  def deleteAll: ZIO[TodoRepository, Nothing, Unit] =
    ZIO.accessM(_.get.deleteAll)

  def update(
    id: TodoId,
    todoItemForm: TodoItemPatchForm
  ): ZIO[TodoRepository, Nothing, Option[TodoItem]] =
    ZIO.accessM(_.get.update(id, todoItemForm))

}
