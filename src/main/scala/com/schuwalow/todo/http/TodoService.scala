package com.schuwalow.todo.http

import com.github.ghik.silencer.silent
import com.github.ghik.silencer.silent
import io.circe.generic.semiauto._
import io.circe.{ Decoder, Encoder }
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio._
import zio.interop.catz._

import com.schuwalow.todo._
import com.schuwalow.todo.repository.TodoRepository
import com.schuwalow.todo.repository._
import com.schuwalow.todo.{
  TodoId,
  TodoItem,
  TodoItemPatchForm,
  TodoItemPostForm
}

object TodoService {

  final case class TodoItemWithUri(
    id: Long,
    url: String,
    title: String,
    completed: Boolean,
    order: Option[Int])

  object TodoItemWithUri {

    def apply(
      basePath: String,
      todoItem: TodoItem
    ): TodoItemWithUri =
      TodoItemWithUri(
        todoItem.id.value,
        s"$basePath/${todoItem.id.value}",
        todoItem.item.title,
        todoItem.item.completed,
        todoItem.item.order
      )

    implicit val encoder: Encoder[TodoItemWithUri] = deriveEncoder
    implicit val decoder: Decoder[TodoItemWithUri] = deriveDecoder
  }

  @silent("unreachable") // https://github.com/scala/bug/issues/11457
  def routes[R <: TodoRepository](rootUri: String): HttpRoutes[RIO[R, ?]] = {
    type TodoTask[A] = RIO[R, A]

    val dsl: Http4sDsl[TodoTask] = Http4sDsl[TodoTask]
    import dsl._

    implicit def circeJsonDecoder[A](
      implicit
      decoder: Decoder[A]
    ): EntityDecoder[TodoTask, A] =
      jsonOf[TodoTask, A]
    implicit def circeJsonEncoder[A](
      implicit
      encoder: Encoder[A]
    ): EntityEncoder[TodoTask, A] =
      jsonEncoderOf[TodoTask, A]

    HttpRoutes.of[TodoTask] {
      case GET -> Root / LongVar(id) =>
        for {
          todo <- getById(TodoId(id))
          response <- todo.fold(NotFound())(
                       x => Ok(TodoItemWithUri(rootUri, x))
                     )
        } yield response

      case GET -> Root =>
        Ok(getAll.map(_.map(TodoItemWithUri(rootUri, _))))

      case req @ POST -> Root =>
        req.decode[TodoItemPostForm] { todoItemForm =>
          create(todoItemForm)
            .map(TodoItemWithUri(rootUri, _))
            .flatMap(Created(_))
        }

      case DELETE -> Root / LongVar(id) =>
        for {
          item <- getById(TodoId(id))
          result <- item
                     .map(x => delete(x.id))
                     .fold(NotFound())(_.flatMap(Ok(_)))
        } yield result

      case DELETE -> Root =>
        deleteAll *> Ok()

      case req @ PATCH -> Root / LongVar(id) =>
        req.decode[TodoItemPatchForm] { updateForm =>
          for {
            update <- update(TodoId(id), updateForm)
            response <- update.fold(NotFound())(
                         x => Ok(TodoItemWithUri(rootUri, x))
                       )
          } yield response
        }
    }
  }
}
