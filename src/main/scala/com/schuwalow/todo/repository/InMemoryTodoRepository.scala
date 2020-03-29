package com.schuwalow.todo.repository

import zio._

import com.schuwalow.todo.{
  TodoId,
  TodoItem,
  TodoItemPatchForm,
  TodoItemPostForm
}

final class InMemoryTodoRepository(
  ref: Ref[Map[TodoId, TodoItem]],
  counter: Ref[Long]) {

  val todoRepository = new TodoRepository.Service {

    override def getAll(): ZIO[Any, Nothing, List[TodoItem]] =
      ref.get.map(_.values.toList)

    override def getById(id: TodoId): ZIO[Any, Nothing, Option[TodoItem]] =
      ref.get.map(_.get(id))

    override def delete(id: TodoId): ZIO[Any, Nothing, Unit] =
      ref.update(store => store - id).unit

    override def deleteAll: ZIO[Any, Nothing, Unit] =
      ref.update(_.empty).unit

    override def create(
      todoItemForm: TodoItemPostForm
    ): ZIO[Any, Nothing, TodoItem] =
      for {
        newId <- counter.updateAndGet(_ + 1).map(TodoId)
        todo  = todoItemForm.asTodoItem(newId)
        _     <- ref.update(store => store + (newId -> todo))
      } yield todo

    override def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
    ): ZIO[Any, Nothing, Option[TodoItem]] =
      for {
        oldValue <- getById(id)
        result <- oldValue.fold[UIO[Option[TodoItem]]](ZIO.succeed(None)) { x =>
                   val newValue = x.update(todoItemForm)
                   ref.update(store => store + (newValue.id -> newValue)) *> ZIO
                     .succeed(
                       Some(newValue)
                     )
                 }
      } yield result
  }
}

object InMemoryTodoRepository {

  val layer: ZLayer[Any, Nothing, TodoRepository] =
    ZLayer.fromEffect {
      for {
        ref     <- Ref.make(Map.empty[TodoId, TodoItem])
        counter <- Ref.make(0L)
      } yield new InMemoryTodoRepository(ref, counter).todoRepository
    }
}
