/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.server.handler

import cats.syntax.all.*

import cats.effect.Async

import io.circe.*
import io.circe.syntax.*

import mcp.schema.McpSchema

import mcp.server.RequestHandler

/**
 * @see https://github.com/modelcontextprotocol/java-sdk/blob/79ec5b5ed1cc1a7abf2edda313a81875bd75ad86/mcp/src/main/java/io/modelcontextprotocol/server/McpAsyncServer.java#L549C73-L549C86
 */
case class ResourcesRead[F[_]: Async](resources: List[McpSchema.ResourceHandler[F]]) extends RequestHandler[F]:

  override def handle(request: Json): F[Either[Throwable, Json]] =
    request.as[McpSchema.ReadResourceRequest] match
      case Left(error) => Async[F].pure(Left(error))
      case Right(request) =>
        resources.find(_.resource match
          case resource: McpSchema.StaticResource => resource.uri == request.uri
          case resource: McpSchema.ResourceTemplate =>
            val templateFormat = resource.uriTemplate.replaceAll("\\{[^}]+\\}", ".*")
            request.uri.matches(templateFormat)) match
          case None => Async[F].pure(Left(new Exception(s"Resource not found: ${ request.uri }")))
          case Some(resource) =>
            resource.readHandler(request).map(result => Right(result.asJson))
