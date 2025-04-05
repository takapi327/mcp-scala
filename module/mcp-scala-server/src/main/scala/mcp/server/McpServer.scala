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

  def connect(transport: McpTransport[F]): McpServer[F]

  def start(): F[Unit]

object McpServer:

  private def voidTransport[F[_]: Async]: McpTransport[F] = new McpTransport[F]:
    override def requestHandlers: Map[String, RequestHandler[F]] = Map.empty
    override def handleRequest(): F[Unit]                        = Async[F].unit

  def apply[F[_]: Async: LiftIO](name: String, version: String): McpServer[F] = Impl[F](
    McpSchema.Implementation(name, version),
    McpSchema.ServerCapabilities(McpSchema.ToolCapabilities(false)),
    List.empty,
    voidTransport
  )

  private case class Impl[F[_]](
    serverInfo:   McpSchema.Implementation,
    capabilities: McpSchema.ServerCapabilities,
    tools:        List[McpSchema.Tool[F, ?]],
    transport:    McpTransport[F]
  ) extends McpServer[F]:

    override def addTool[T](tool: McpSchema.Tool[F, T]): McpServer[F] =
      this.copy(tools = tools :+ tool)

    override def connect(transport: McpTransport[F]): McpServer[F] =
      this.copy(transport = transport)

    override def start(): F[Unit] =
      transport.handleRequest()

  case class FastMcp[F[_]: Async: LiftIO](
    serverInfo:   McpSchema.Implementation,
    capabilities: McpSchema.ServerCapabilities,
    tools:        List[McpSchema.Tool[F, ?]]
  ):

    private def handleProvider: RequestHandler.Provider[F] = new RequestHandler.Provider[F](
      serverInfo,
      capabilities,
      tools
    )

    def addTool[T](tool: McpSchema.Tool[F, T]): FastMcp[F] =
      this.copy(tools = tools :+ tool)

    def start(transportType: "stdio" | "sse"): F[Unit] =
      transportType match
        case "stdio" => StdioMcpTransport(handleProvider.handlers, None, None).handleRequest()
        case "sse"   => Async[F].raiseError(new McpError("SSE transport is not implemented yet"))

  object FastMcp:

    def apply[F[_]: Async: LiftIO](name: String, version: String): FastMcp[F] = FastMcp[F](
      McpSchema.Implementation(name, version),
      McpSchema.ServerCapabilities(McpSchema.ToolCapabilities(false)),
      List.empty
    )
