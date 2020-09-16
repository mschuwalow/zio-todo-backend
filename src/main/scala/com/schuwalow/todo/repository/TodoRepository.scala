package com.schuwalow.todo.repository

import zio._
import zio.logging.Logging
import zio.logging.log

import com.schuwalow.todo.{ TodoId, TodoItem, TodoItemPatchForm, TodoItemPostForm }

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

  def create(todoItemForm: TodoItemPostForm): URIO[TodoRepository, TodoItem] = ZIO.accessM(_.get.create(todoItemForm))

  def getById(id: TodoId): URIO[TodoRepository, Option[TodoItem]] = ZIO.accessM(_.get.getById(id))

  val getAll: URIO[TodoRepository, List[TodoItem]] =
    ZIO.accessM(_.get.getAll)

  def delete(id: TodoId): URIO[TodoRepository, Unit] = ZIO.accessM(_.get.delete(id))

  val deleteAll: URIO[TodoRepository, Unit] =
    ZIO.accessM(_.get.deleteAll)

  def update(
    id: TodoId,
    todoItemForm: TodoItemPatchForm
  ): URIO[TodoRepository, Option[TodoItem]] = ZIO.accessM(_.get.update(id, todoItemForm))

  def withTracing[RIn, ROut <: TodoRepository with Logging, E](
    layer: ZLayer[RIn, E, ROut]
  ): ZLayer[RIn, E, ROut] =
    layer >>> ZLayer.fromFunctionMany[ROut, ROut] { env =>
      def trace(call: => String) = log.trace(s"TodoRepository.$call")

      env.update[TodoRepository.Service] { service =>
        new Service {
          val getAll: UIO[List[TodoItem]] =
            (trace("getAll") *> service.getAll).provide(env)

          def getById(id: TodoId): UIO[Option[TodoItem]] = (trace(s"getById($id)") *> service.getById(id)).provide(env)

          def delete(id: TodoId): UIO[Unit] = (trace(s"delete($id)") *> service.delete(id)).provide(env)

          val deleteAll: UIO[Unit] =
            (trace("deleteAll") *> service.deleteAll).provide(env)

          def create(todoItemForm: TodoItemPostForm): UIO[TodoItem] =
            (trace(s"create($todoItemForm)") *> service.create(todoItemForm))
              .provide(env)

          def update(
            id: TodoId,
            todoItemForm: TodoItemPatchForm
          ): UIO[Option[TodoItem]] =
            (trace(s"update($id, $todoItemForm)") *> service.update(
              id,
              todoItemForm
            )).provide(env)
        }
      }
    }
}
