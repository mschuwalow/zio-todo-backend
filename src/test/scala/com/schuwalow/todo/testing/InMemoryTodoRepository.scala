package com.schuwalow.todo.testing

import zio._

import com.schuwalow.todo._
import com.schuwalow.todo.repository.TodoRepository

final private class InMemoryTodoRepository(
  ref: Ref[Map[TodoId, TodoItem]],
  counter: Ref[Long])
    extends TodoRepository.Service {

  override def getAll: UIO[List[TodoItem]] = ref.get.map(_.values.toList)

  override def getById(id: TodoId): UIO[Option[TodoItem]] = ref.get.map(_.get(id))

  override def delete(id: TodoId): UIO[Unit] = ref.update(store => store - id).unit

  override def deleteAll: UIO[Unit] = ref.update(_.empty).unit

  override def create(todoItemForm: TodoItemPostForm): UIO[TodoItem] =
    for {
      newId <- counter.updateAndGet(_ + 1).map(TodoId)
      todo   = todoItemForm.asTodoItem(newId)
      _     <- ref.update(store => store + (newId -> todo))
    } yield todo

  override def update(
    id: TodoId,
    todoItemForm: TodoItemPatchForm
  ): UIO[Option[TodoItem]] =
    for {
      oldValue <- getById(id)
      result   <- oldValue.fold[UIO[Option[TodoItem]]](ZIO.succeed(None)) { x =>
                    val newValue = x.update(todoItemForm)
                    ref.update(store => store + (newValue.id -> newValue)) *> ZIO
                      .succeed(
                        Some(newValue)
                      )
                  }
    } yield result
}

object InMemoryTodoRepository {

  val layer: ZLayer[Any, Nothing, TodoRepository] =
    ZLayer.fromEffect {
      for {
        ref     <- Ref.make(Map.empty[TodoId, TodoItem])
        counter <- Ref.make(0L)
      } yield new InMemoryTodoRepository(ref, counter)
    }
}
