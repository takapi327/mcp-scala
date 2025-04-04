/**
 * Copyright (c) 2023-2024 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.server

import cats.syntax.all.*

import cats.effect.*

import fs2.*
import fs2.io.*

import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*

import mcp.schema.McpSchema

case class StdioMcpTransport[F[_]: Async](
  requestHandlers: Map[String, RequestHandler[F]],
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
        val response = McpSchema.JSONRPCResponse.failure(
          McpSchema.JSONRPCError(
            McpSchema.ErrorCodes.PARSE_ERROR,
            error.getMessage,
            None
          )
        )
        Async[F].pure(response.asJson.noSpaces)
      case Right(json) =>
        json.as[McpSchema.JSONRPCMessage] match
          case Left(error) =>
            val response = McpSchema.JSONRPCResponse.failure(
              McpSchema.JSONRPCError(
                McpSchema.ErrorCodes.INVALID_REQUEST,
                error.getMessage,
                None
              )
            )
            Async[F].pure(response.asJson.noSpaces)
          case Right(request) =>
            (request match
              case req: McpSchema.JSONRPCRequest               => handleIncomingRequest(req)
              case notification: McpSchema.JSONRPCNotification => handleIncomingNotification(notification)
              case batch: McpSchema.JSONRPCBatch               => handleIncomingBatch(batch)
              case _: McpSchema.JSONRPCResponse =>
                val response = McpSchema.JSONRPCResponse.failure(
                  McpSchema.JSONRPCError(
                    McpSchema.ErrorCodes.INVALID_REQUEST,
                    "Invalid Request",
                    None
                  )
                )
                Async[F].pure(response)
            ).map(_.asJson.noSpaces)

  private def handleIncomingRequest(request: McpSchema.JSONRPCRequest): F[McpSchema.JSONRPCResponse] =
    requestHandlers.get(request.method) match
      case None =>
        val response = McpSchema.JSONRPCResponse.failure(
          request.id,
          McpSchema.JSONRPCError(
            McpSchema.ErrorCodes.METHOD_NOT_FOUND,
            s"Method not found: ${ request.method }",
            None
          )
        )
        Async[F].pure(response)
      case Some(handler) =>
        handler.handle(request.params.getOrElse(Json.Null)).map {
          case Left(error) =>
            McpSchema.JSONRPCResponse.failure(
              request.id,
              McpSchema.JSONRPCError(
                McpSchema.ErrorCodes.INTERNAL_ERROR,
                error.getMessage,
                None
              )
            )
          case Right(result) => McpSchema.JSONRPCResponse.success(request.id, result)
        }

  private def handleIncomingNotification(notification: McpSchema.JSONRPCNotification): F[McpSchema.JSONRPCResponse] =
    val response = McpSchema.JSONRPCResponse.success(
      McpSchema.JSONRPCRequest.Id.NullId,
      Json.fromString("Notification received")
    )
    requestHandlers.get(notification.method) match
      case None => Async[F].pure(response)
      case Some(handler) =>
        handler.handle(notification.params.getOrElse(Json.Null)).map { _ =>
          response
        }

  private def handleIncomingBatch(batch: McpSchema.JSONRPCBatch): F[McpSchema.JSONRPCResponse] =
    if batch.requests.isEmpty then
      Async[F].pure(
        McpSchema.JSONRPCResponse.failure(
          McpSchema.JSONRPCError(
            McpSchema.ErrorCodes.INVALID_REQUEST,
            "Empty batch",
            None
          )
        )
      )
    else
      val listIO = batch.requests.map {
        case req: McpSchema.JSONRPCRequest               => handleIncomingRequest(req).map(Some(_))
        case notification: McpSchema.JSONRPCNotification => handleIncomingNotification(notification).map(_ => None)
        case _ =>
          Async[F].pure(
            Some(
              McpSchema.JSONRPCResponse.failure(
                McpSchema.JSONRPCError(
                  McpSchema.ErrorCodes.INVALID_REQUEST,
                  "Invalid Request",
                  None
                )
              )
            )
          )
      }
      listIO.sequence.map { responses =>
        val filteredResponses = responses.flatten
        if filteredResponses.isEmpty then
          McpSchema.JSONRPCResponse.success(
            McpSchema.JSONRPCRequest.Id.NullId,
            Json.fromString("Batch processed")
          )
        else
          McpSchema.JSONRPCResponse.success(
            McpSchema.JSONRPCRequest.Id.NullId,
            Json.obj("responses" -> filteredResponses.asJson)
          )
      }
