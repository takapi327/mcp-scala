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

import mcp.schema.*
import mcp.schema.handler.*
import mcp.schema.result.*

import mcp.server.RequestHandler

/**
 * @see https://github.com/modelcontextprotocol/java-sdk/blob/79ec5b5ed1cc1a7abf2edda313a81875bd75ad86/mcp/src/main/java/io/modelcontextprotocol/server/McpAsyncServer.java#L533
 */
case class ResourcesList[F[_]: Async](resources: List[ResourceHandler[F]]) extends RequestHandler[F]:

  override def handle(request: Json): F[Either[Throwable, Json]] =
    val statics = resources.map(_.resource).filter(_.isStatic)
    Async[F].pure(Right(ListResourcesResult(statics, None).asJson))
