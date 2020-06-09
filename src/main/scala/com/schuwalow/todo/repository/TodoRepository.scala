package com.schuwalow.todo.repository

import zio._
import zio.logging.Logging
import zio.logging.Logging.Logging

import com.schuwalow.todo.{
  TodoId,
  TodoItem,
  TodoItemPatchForm,
  TodoItemPostForm
}

object TodoRepository extends Serializable {

  trait Service extends Serializable {

    def getAll: UIO[List[TodoItem]]

    def getById(id: TodoId): UIO[Option[TodoItem]]

    def delete(id: TodoId): UIO[Unit]

    def deleteAll: UIO[Unit]

    def create(todoItemForm: TodoItemPostForm): UIO[TodoItem]

    def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
    ): UIO[Option[TodoItem]]
  }

  def withTracing[RIn, ROut <: TodoRepository with Logging, E](
    layer: ZLayer[RIn, E, ROut]
  ): ZLayer[RIn, E, ROut] =
    layer >>> ZLayer.fromFunctionMany[ROut, ROut] { env =>
      val logging                = env.get[Logging.Service]
      def trace(call: => String) = logging.logger.trace(s"TodoRepository.$call")

      env.update[TodoRepository.Service] { service =>
        new Service {
          val getAll: UIO[List[TodoItem]] =
            trace("getAll") *> service.getAll

          def getById(id: TodoId): UIO[Option[TodoItem]] =
            trace(s"getById($id)") *> service.getById(id)

          def delete(id: TodoId): UIO[Unit] =
            trace(s"delete($id)") *> service.delete(id)

          val deleteAll: UIO[Unit] =
            trace("deleteAll") *> service.deleteAll

          def create(todoItemForm: TodoItemPostForm): UIO[TodoItem] =
            trace(s"create($todoItemForm)") *> service.create(todoItemForm)

          def update(
            id: TodoId,
            todoItemForm: TodoItemPatchForm
          ): UIO[Option[TodoItem]] =
            trace(s"update($id, $todoItemForm)") *> service.update(
              id,
              todoItemForm
            )
        }
      }
    }
}
