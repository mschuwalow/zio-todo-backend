package com.schuwalow.zio.todo.repository

import com.schuwalow.zio.todo.{TodoItem, TodoItemPostForm, TodoItemPatchForm}
import scalaz.zio._

trait TodoRepository extends Serializable {

  val todoRepository: TodoRepository.Service[Any]

}

object TodoRepository extends Serializable {

  trait Service[R] extends Serializable {

    def getAll: ZIO[R, Nothing, Seq[TodoItem]]

    def getById(id: Long): ZIO[R, Nothing, Option[TodoItem]]

    def delete(id: Long): ZIO[R, Nothing, Unit]

    def deleteAll: ZIO[R, Nothing, Unit]

    def create(todoItemForm: TodoItemPostForm): ZIO[R, Nothing, TodoItem]

    def update(id: Long, todoItemForm: TodoItemPatchForm): ZIO[R, Nothing, Option[TodoItem]]

  }

  final case class InMemoryTodoRepository(ref: Ref[Map[Long, TodoItem]], counter: Ref[Long]) extends Service[Any] {

    override def getAll: ZIO[Any, Nothing, Seq[TodoItem]] =
      ref.get.map(_.values.toSeq)

    override def getById(id: Long): ZIO[Any, Nothing, Option[TodoItem]] =
      ref.get.map(_.get(id))

    override def delete(id: Long): ZIO[Any, Nothing, Unit] =
      ref.update(store => store - id).void

    override def deleteAll: ZIO[Any, Nothing, Unit] =
      ref.update(_.empty).void

    override def create(todoItemForm: TodoItemPostForm): ZIO[Any, Nothing, TodoItem] =
      for {
        newId  <- counter.update(_ + 1)
        todo    = todoItemForm.asTodoItem(newId)
        _      <- ref.update(store => store + (newId -> todo))
      } yield todo

    override def update(id: Long, todoItemForm: TodoItemPatchForm): ZIO[Any, Nothing, Option[TodoItem]] =
      for {
        oldValue <- getById(id)
        result   <- oldValue.fold[UIO[Option[TodoItem]]](ZIO.succeed(None)) { x =>
          val newValue = x.update(todoItemForm)
          ref.update(store => store + (newValue.id -> newValue)) *> ZIO.succeed(Some(newValue))
        }
      } yield result

  }

}
