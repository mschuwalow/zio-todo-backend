package com.schuwalow.todo.http

import org.http4s.{ EntityDecoder, Method, Request, Response, Status, Uri }
import zio._
import zio.interop.catz._
import zio.test.Assertion._
import zio.test._

object HTTPSpec {

  def request[F[_]](
    method: Method,
    uri: String
  ): Request[F] =
    Request(method = method, uri = Uri.fromString(uri).toOption.get)

  def checkRequest[R, A](
    actual: RIO[R, Response[RIO[R, ?]]],
    expectedStatus: Status,
    expectedBody: Option[A]
  )(implicit
    ev: EntityDecoder[RIO[R, ?], A]
  ): RIO[R, TestResult] =
    for {
      actual <- actual
      bodyResult <- expectedBody
                     .fold[RIO[R, TestResult]](
                       assertM(actual.bodyText.compile.toVector)(isEmpty)
                     )(
                       expected => assertM(actual.as[A])(equalTo(expected))
                     )
      statusResult = assert(actual.status)(equalTo(expectedStatus))
    } yield bodyResult && statusResult

  def checkRequestRaw[R, A](
    actual: RIO[R, Response[RIO[R, ?]]],
    expectedStatus: Status,
    expectedBody: String
  ): RIO[R, TestResult] =
    checkRequest(actual, expectedStatus, Some(expectedBody))
}
