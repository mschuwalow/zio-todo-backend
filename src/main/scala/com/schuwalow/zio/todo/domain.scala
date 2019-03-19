package com.schuwalow.zio.todo

final case class TodoItem(id: Long, title: String, completed: Boolean, order: Option[Int]) {

  def update(form: TodoItemPatchForm): TodoItem =
    this.copy(title       = form.title.getOrElse(title),
              completed   = form.completed.getOrElse(completed),
              order       = form.order.orElse(order))

}

final case class TodoItemWithUri(url: String, id: Long, title: String, completed: Boolean, order: Option[Int])

object TodoItemWithUri {

  def apply(basePath: String, payload: TodoItem): TodoItemWithUri =
    TodoItemWithUri(s"$basePath/${payload.id}", payload.id, payload.title, payload.completed, payload.order)

}

final case class TodoItemPostForm(title: String, order: Option[Int] = None) {

  def asTodoItem(id: Long): TodoItem =
    TodoItem(id, title, false, order)

}

final case class TodoItemPatchForm(title: Option[String], completed: Option[Boolean], order: Option[Int])
