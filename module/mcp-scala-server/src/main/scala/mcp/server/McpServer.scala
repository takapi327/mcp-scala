/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.server

import cats.effect.*

import mcp.schema.*
import mcp.schema.handler.*

trait McpServer[F[_]]:

  def serverInfo: Implementation

  def capabilities: ServerCapabilities

  def addTool[T](tool: Tool[F, T]): McpServer[F]

  def addResource(resource: ResourceHandler[F]): McpServer[F]

  def addPrompt(prompt: PromptHandler[F]): McpServer[F]

  def setCapabilities(capabilities: ServerCapabilities): McpServer[F]

  def connect(transport: McpTransport[F]): McpServer[F]

  def start(): F[Unit]

object McpServer:

  private def voidTransport[F[_]: Async]: McpTransport[F] = new McpTransport[F]:
    override def requestHandlers: Map[Method, RequestHandler[F]] = Map.empty
    override def handleRequest(): F[Unit]                        = Async[F].unit

  def apply[F[_]: Async: LiftIO](name: String, version: String): McpServer[F] = Impl[F](
    Implementation(name, version),
    ServerCapabilities.build()
      .withPromptsListChanged(false)
      .withToolsListChanged(false),
    List.empty,
    List.empty,
    List.empty,
    voidTransport
  )

  private case class Impl[F[_]](
    serverInfo:   Implementation,
    capabilities: ServerCapabilities,
    tools:        List[Tool[F, ?]],
    resources:    List[ResourceHandler[F]],
    prompts:      List[PromptHandler[F]],
    transport:    McpTransport[F]
  ) extends McpServer[F]:

    override def addTool[T](tool: Tool[F, T]): McpServer[F] =
      this.copy(tools = tools :+ tool)

    override def addResource(resource: ResourceHandler[F]): McpServer[F] =
      this.copy(resources = resources :+ resource)

    override def addPrompt(prompt: PromptHandler[F]): McpServer[F] =
      this.copy(prompts = prompts :+ prompt)

    override def setCapabilities(capabilities: ServerCapabilities): McpServer[F] =
      this.copy(capabilities = capabilities)

    override def connect(transport: McpTransport[F]): McpServer[F] =
      this.copy(transport = transport)

    override def start(): F[Unit] =
      transport.handleRequest()

  case class FastMcp[F[_]: Async: LiftIO](
    serverInfo:   Implementation,
    capabilities: ServerCapabilities,
    tools:        List[Tool[F, ?]],
    resources:    List[ResourceHandler[F]],
    prompts:      List[PromptHandler[F]],
    handlers:     Map[Method, RequestHandler[F]]
  ):

    private def handleProvider: RequestHandler.Provider[F] = new RequestHandler.Provider[F](
      serverInfo,
      capabilities,
      tools,
      resources,
      prompts
    )

    def addTool[T](tool: Tool[F, T]): FastMcp[F] =
      this.copy(tools = tools :+ tool)

    def addResource(resource: ResourceHandler[F]): FastMcp[F] =
      this.copy(resources = resources :+ resource)

    def addPrompt(prompt: PromptHandler[F]): FastMcp[F] =
      this.copy(prompts = prompts :+ prompt)

    def setCapabilities(capabilities: ServerCapabilities): FastMcp[F] =
      this.copy(capabilities = capabilities)

    def setRequestHandler(method: Method, requestHandler: RequestHandler[F]): FastMcp[F] =
      this.copy(handlers = handlers + (method -> requestHandler))

    def start(transportType: "stdio" | "sse"): F[Unit] =
      transportType match
        case "stdio" =>
          StdioMcpTransport(
            handleProvider.handlers,
            Some("/Users/takapi327/Development/oss/scala/mcp-scala/input.log"),
            Some("/Users/takapi327/Development/oss/scala/mcp-scala/output.log")
          ).handleRequest()
        case "sse" => Async[F].raiseError(new McpError("SSE transport is not implemented yet"))

  object FastMcp:

    def apply[F[_]: Async: LiftIO](name: String, version: String): FastMcp[F] =
      val serverInfo = Implementation(name, version)
      val capabilities = ServerCapabilities.build()
        .withPromptsListChanged(false)
        .withToolsListChanged(false)
      val handleProvider = new RequestHandler.Provider[F](
        serverInfo,
        capabilities,
        List.empty,
        List.empty,
        List.empty
      )
      FastMcp[F](
        serverInfo,
        capabilities,
        List.empty,
        List.empty,
        List.empty,
        handleProvider.handlers
      )
