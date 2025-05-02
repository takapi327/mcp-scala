/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.server

import cats.syntax.all.*

import cats.effect.*

import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*

import fs2.{ text, Stream }
import fs2.io.{ file, stdin, stdout }

import mcp.schema.*

case class StdioMcpTransport[F[_]: Async: LiftIO](
  requestHandlers: Map[Method, RequestHandler[F]],
  inputLogFile:    Option[String] = None,
  outputLogFile:   Option[String] = None
) extends McpTransport[F]:

  override def handleRequest(): F[Unit] =
    // Stream to read standard input
    val inputStream = stdin[F](4096)
      .through(text.utf8.decode)
      .through(text.lines)

    // Create a pipeline to write input to a file (optional)
    val inputStreamWithLogging = inputLogFile match
      case Some(path) =>
        inputStream.evalTap(line =>
          file.Files
            .forAsync[F]
            .writeUtf8(
              file.Path(path),
              file.Flags.Append
            )
            .apply(Stream.emit(s"$line\n"))
            .compile
            .drain
        )
      case None => inputStream

    // Pipeline for request processing and output
    val processedStream = inputStreamWithLogging
      .evalMap(handleJsonRequest)
      .map(result => s"$result\n")

    // Create a pipeline to write output to a file (optional)
    val outputStreamWithLogging = outputLogFile match
      case Some(path) =>
        processedStream.evalTap(result =>
          file.Files
            .forAsync[F]
            .writeUtf8(
              file.Path(path),
              file.Flags.Append
            )
            .apply(Stream.emit(result))
            .compile
            .drain
        )
      case None => processedStream

    // Write to standard output
    outputStreamWithLogging
      .through(text.utf8.encode)
      .through(stdout[F])
      .compile
      .drain

  def handleJsonRequest(body: String): F[String] =
    parse(body) match
      case Left(error) =>
        val response = JSONRPCResponse.failure(
          RequestId.StringId("Empty Request Id"),
          ErrorCodes.PARSE_ERROR,
          error.getMessage,
          None
        )
        Async[F].pure(response.asJson.noSpaces)
      case Right(json) =>
        json.as[JSONRPCMessage] match
          case Left(error) =>
            val id = json.hcursor.get[RequestId]("id").getOrElse(throw new IllegalArgumentException("The required Id does not exist."))
            val response = JSONRPCResponse.failure(
              id,
              ErrorCodes.INVALID_REQUEST,
              error.getMessage,
              None
            )
            Async[F].pure(response.asJson.noSpaces)
          case Right(request) =>
            request match
              case req: JSONRPCRequest               => handleIncomingRequest(req).map(_.asJson.noSpaces)
              case notification: JSONRPCNotification => handleIncomingNotification(notification).map(_.asJson.noSpaces)
              case batch: JSONRPCBatch               => handleIncomingBatch(batch).map(_.asJson.noSpaces)
              case res: JSONRPCResponse =>
                val response = JSONRPCResponse.failure(
                  res.id,
                  ErrorCodes.INVALID_REQUEST,
                  "Invalid Request",
                  None
                )
                Async[F].pure(response.asJson.noSpaces)

  private def handleIncomingRequest(request: JSONRPCRequest): F[JSONRPCResponse] =
    requestHandlers.get(request.method) match
      case None =>
        val response = JSONRPCResponse.failure(
          request.id,
          ErrorCodes.METHOD_NOT_FOUND,
          s"Method not found: ${ request.method }",
          None
        )
        Async[F].pure(response)
      case Some(handler) =>
        handler.handle(request.params.getOrElse(Json.Null)).map {
          case Left(error) =>
            JSONRPCResponse.failure(
              request.id,
              ErrorCodes.INTERNAL_ERROR,
              error.getMessage,
              None
            )
          case Right(result) => JSONRPCResponse.success(request.id, result)
        }

  private def handleIncomingNotification(notification: JSONRPCNotification): F[JSONRPCResponse] =
    requestHandlers.get(notification.method) match
      case None => Async[F].pure(JSONRPCResponse.success(RequestId.NumberId(0), Json.obj()))
      case Some(handler) =>
        handler.handle(notification.params.getOrElse(Json.Null)).map { _ =>
          JSONRPCResponse.success(RequestId.NumberId(0), Json.obj())
        }

  private def handleIncomingBatch(batch: JSONRPCBatch): F[List[JSONRPCResponse]] =
    val listIO = batch.requests.map {
      case req: JSONRPCRequest               => handleIncomingRequest(req).map { response =>
        Some(JSONRPCResponse.success(req.id, response.asJson))
      }
      case notification: JSONRPCNotification => handleIncomingNotification(notification).map(_ => None)
      case _ => Async[F].pure(None)
    }
    listIO.sequence.map(_.flatten)
