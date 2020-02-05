package com.schuwalow.todo.repository

import zio._
import com.schuwalow.todo.{TodoId, TodoItem, TodoItemPatchForm, TodoItemPostForm}

trait TodoRepository extends Serializable {

  val todoRepository: TodoRepository.Service[Any]

}

object TodoRepository extends Serializable {

  trait Service[R] extends Serializable {

    def getAll(): ZIO[R, Nothing, List[TodoItem]]

    def getById(id: TodoId): ZIO[R, Nothing, Option[TodoItem]]

    def delete(id: TodoId): ZIO[R, Nothing, Unit]

    def deleteAll: ZIO[R, Nothing, Unit]

    def create(todoItemForm: TodoItemPostForm): ZIO[R, Nothing, TodoItem]

    def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
    ): ZIO[R, Nothing, Option[TodoItem]]
  }
}
