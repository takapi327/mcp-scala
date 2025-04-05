/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.server

import cats.effect.*

import mcp.schema.{ McpError, McpSchema }

trait McpServer[F[_]]:

  def serverInfo: McpSchema.Implementation

  def capabilities: McpSchema.ServerCapabilities

  def addTool[T](tool: McpSchema.Tool[F, T]): McpServer[F]

  def start(transportType: "stdio" | "sse"): F[Unit]

object McpServer:

  case class Imp[F[_]: Async: LiftIO](
    serverInfo:   McpSchema.Implementation,
    capabilities: McpSchema.ServerCapabilities,
    tools:        List[McpSchema.Tool[F, ?]]
  ) extends McpServer[F]:

    private def handleProvider: RequestHandler.Provider[F] = new RequestHandler.Provider[F](
      serverInfo,
      capabilities,
      tools
    )

    override def addTool[T](tool: McpSchema.Tool[F, T]): McpServer[F] =
      this.copy(tools = tools :+ tool)

    override def start(transportType: "stdio" | "sse"): F[Unit] =
      transportType match
        case "stdio" => StdioMcpTransport(handleProvider.handlers, None, None).handleRequest()
        case "sse"   => Async[F].raiseError(new McpError("SSE transport is not implemented yet"))

  def apply[F[_]: Async: LiftIO](name: String, version: String): McpServer[F] = Imp[F](
    McpSchema.Implementation(name, version),
    McpSchema.ServerCapabilities(McpSchema.ToolCapabilities(false)),
    List.empty
  )
