package com.schuwalow.todo

import zio._

package object repository {
  type TodoRepository = Has[TodoRepository.Service]

  def create(todoItemForm: TodoItemPostForm): URIO[TodoRepository, TodoItem] =
    ZIO.accessM(_.get.create(todoItemForm))

  def getById(id: TodoId): URIO[TodoRepository, Option[TodoItem]] =
    ZIO.accessM(_.get.getById(id))

  def getAll: URIO[TodoRepository, List[TodoItem]] =
    ZIO.accessM(_.get.getAll)

  def delete(id: TodoId): URIO[TodoRepository, Unit] =
    ZIO.accessM(_.get.delete(id))

  def deleteAll: URIO[TodoRepository, Unit] =
    ZIO.accessM(_.get.deleteAll)

  def update(
    id: TodoId,
    todoItemForm: TodoItemPatchForm
  ): URIO[TodoRepository, Option[TodoItem]] =
    ZIO.accessM(_.get.update(id, todoItemForm))
}
