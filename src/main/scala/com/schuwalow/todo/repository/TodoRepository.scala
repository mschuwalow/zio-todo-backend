package com.schuwalow.todo.repository

import zio._

import com.schuwalow.todo.{
  TodoId,
  TodoItem,
  TodoItemPatchForm,
  TodoItemPostForm
}

object TodoRepository extends Serializable {

  trait Service extends Serializable {

    def getAll(): ZIO[Any, Nothing, List[TodoItem]]

    def getById(id: TodoId): ZIO[Any, Nothing, Option[TodoItem]]

    def delete(id: TodoId): ZIO[Any, Nothing, Unit]

    def deleteAll: ZIO[Any, Nothing, Unit]

    def create(todoItemForm: TodoItemPostForm): ZIO[Any, Nothing, TodoItem]

    def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
    ): ZIO[Any, Nothing, Option[TodoItem]]
  }
}
