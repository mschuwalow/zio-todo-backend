package com.schuwalow.zio.todo

import cats.effect.Sync
import cats.implicits._
import org.http4s.{EntityDecoder, Method, Request, Response, Status, Uri}
import org.scalatest.{FunSpec, OptionValues}

abstract class UnitSpec extends FunSpec with OptionValues {}

object UnitSpec {

  def request[F[_]](method: Method, uri: String): Request[F] =
    Request(method = method, uri = Uri.fromString(uri).toOption.get)

  def check[F[_]: Sync, A](
    actual:          F[Response[F]],
    expectedStatus:  Status,
    expectedBody:    Option[A]
  )(implicit
    ev: EntityDecoder[F, A]
  ): F[Boolean] =  {
    for {
      actual       <- actual
      bodyCheck    <- expectedBody.fold[F[Boolean]](
        actual.body.compile.toVector.map(_.isEmpty))(
        expected => actual.as[A].map(_ == expected)
      )
      statusCheck   = actual.status == expectedStatus
    } yield statusCheck && bodyCheck
  }

}
