/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.server

trait McpTransport[F[_]]:

  def requestHandlers: Map[String, RequestHandler[F]]

  def handleRequest(): F[Unit]
