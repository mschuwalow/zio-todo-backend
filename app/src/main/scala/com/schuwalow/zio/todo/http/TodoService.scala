package com.schuwalow.zio.todo.http

import com.schuwalow.zio.todo._
import com.schuwalow.zio.todo.repository._
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import scalaz.zio.TaskR
import scalaz.zio.interop.catz._

final case class TodoService[R <: TodoRepository](rootUri: String) {
  import TodoService._

  type TodoTask[A] = TaskR[R, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[TodoTask, A] = jsonOf[TodoTask, A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[TodoTask, A] = jsonEncoderOf[TodoTask, A]

  val dsl: Http4sDsl[TodoTask] = Http4sDsl[TodoTask]
  import dsl._

  def service: HttpRoutes[TodoTask] = {

    HttpRoutes.of[TodoTask] {

      case GET -> Root =>
        Ok(getAll.map(_.map(TodoItemWithUri(rootUri, _))))

      case GET -> Root / LongVar(id) =>
        for {
          todo     <- getById(TodoId(id))
          response <- todo.fold(NotFound())(x => Ok(TodoItemWithUri(rootUri, x)))
        } yield response

      case req @ POST -> Root =>
        req.decode[TodoItemPostForm] { todoItemForm =>
          Created(create(todoItemForm).map(TodoItemWithUri(rootUri, _)))
        }

      case DELETE -> Root / LongVar(id) =>
        for {
          item     <- getById(TodoId(id))
          result   <- item.map(x => delete(x.id)).fold(NotFound())(Ok(_))
        } yield result

      case DELETE -> Root =>
        deleteAll *> Ok()

      case req @ PATCH -> Root / LongVar(id) =>
        req.decode[TodoItemPatchForm] { updateForm =>
          for {
            update   <- update(TodoId(id), updateForm)
            response <- update.fold(NotFound())(x => Ok(TodoItemWithUri(rootUri, x)))
          } yield response
        }
    }
  }
}

object TodoService {

  final case class TodoItemWithUri(
    id: Long,
    url: String,
    title: String,
    completed: Boolean,
    order: Option[Int]
  )

  object TodoItemWithUri {

    def apply(basePath: String, todoItem: TodoItem): TodoItemWithUri =
      TodoItemWithUri(todoItem.id.value, s"$basePath/${todoItem.id.value}", todoItem.item.title, todoItem.item.completed, todoItem.item.order)

  }

}
