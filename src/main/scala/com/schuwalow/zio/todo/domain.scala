package com.schuwalow.zio.todo

final case class TodoId(value: Long) extends AnyVal

final case class TodoPayload(
  title: String,
  completed: Boolean,
  order: Option[Int]
)

final case class TodoItem(
  id: TodoId,
  item: TodoPayload
) {

  def update(form: TodoItemPatchForm): TodoItem =
    this.copy(
      id    = this.id,
      item  = item.copy(
                title       = form.title.getOrElse(item.title),
                completed   = form.completed.getOrElse(item.completed),
                order       = form.order.orElse(item.order)
              )
    )
}

final case class TodoItemPostForm(
  title: String,
  order: Option[Int] = None
) {

  def asTodoItem(id: TodoId): TodoItem =
    TodoItem(id, this.asTodoPayload)

  def asTodoPayload: TodoPayload =
    TodoPayload(title, false, order)

}

final case class TodoItemPatchForm(
  title: Option[String] = None,
  completed: Option[Boolean] = None,
  order: Option[Int] = None
)
