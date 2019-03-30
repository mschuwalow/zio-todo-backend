package com.schuwalow.zio.todo

import scalaz.zio._

package object repository extends TodoRepository.Service[TodoRepository] {

  override def create(todoItemForm: TodoItemPostForm): ZIO[TodoRepository, Nothing, TodoItem] = ZIO.accessM(_.todoRepository.create(todoItemForm))

  override def getById(id: Long): ZIO[TodoRepository, Nothing, Option[TodoItem]] = ZIO.accessM(_.todoRepository.getById(id))

  override def getAll: ZIO[TodoRepository, Nothing, Seq[TodoItem]] = ZIO.accessM(_.todoRepository.getAll)

  override def delete(id: Long): ZIO[TodoRepository, Nothing, Unit] = ZIO.accessM(_.todoRepository.delete(id))

  override def deleteAll: ZIO[TodoRepository, Nothing, Unit] = ZIO.accessM(_.todoRepository.deleteAll)

  override def update(id: FiberId, todoItemForm: TodoItemPatchForm): ZIO[TodoRepository, Nothing, Option[TodoItem]] = ZIO.accessM(_.todoRepository.update(id, todoItemForm))

}
