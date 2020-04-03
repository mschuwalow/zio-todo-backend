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

    def getAll(): UIO[List[TodoItem]]

    def getById(id: TodoId): UIO[Option[TodoItem]]

    def delete(id: TodoId): UIO[Unit]

    def deleteAll: UIO[Unit]

    def create(todoItemForm: TodoItemPostForm): UIO[TodoItem]

    def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
    ): UIO[Option[TodoItem]]
  }
}
