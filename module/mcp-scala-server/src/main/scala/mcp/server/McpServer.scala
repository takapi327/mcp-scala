/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.server

import cats.effect.*

import mcp.schema.*

trait McpServer[F[_]]:

  def serverInfo: McpSchema.Implementation

  def capabilities: McpSchema.ServerCapabilities

  def addTool[T](tool: McpSchema.Tool[F, T]): McpServer[F]

  def addResource(resource: McpSchema.ResourceHandler[F]): McpServer[F]

  def addPrompt(prompt: McpSchema.PromptHandler[F]): McpServer[F]

  def setCapabilities(capabilities: McpSchema.ServerCapabilities): McpServer[F]

  def connect(transport: McpTransport[F]): McpServer[F]

  def start(): F[Unit]

object McpServer:

  private def voidTransport[F[_]: Async]: McpTransport[F] = new McpTransport[F]:
    override def requestHandlers: Map[Method, RequestHandler[F]] = Map.empty
    override def handleRequest(): F[Unit]                        = Async[F].unit

  def apply[F[_]: Async: LiftIO](name: String, version: String): McpServer[F] = Impl[F](
    McpSchema.Implementation(name, version),
    McpSchema.ServerCapabilities(
      PromptCapabilities(false),
      ResourceCapabilities(None, None),
      McpSchema.ToolCapabilities(false)
    ),
    List.empty,
    List.empty,
    List.empty,
    voidTransport
  )

  private case class Impl[F[_]](
    serverInfo:   McpSchema.Implementation,
    capabilities: McpSchema.ServerCapabilities,
    tools:        List[McpSchema.Tool[F, ?]],
    resources:    List[McpSchema.ResourceHandler[F]],
    prompts:      List[McpSchema.PromptHandler[F]],
    transport:    McpTransport[F]
  ) extends McpServer[F]:

    override def addTool[T](tool: McpSchema.Tool[F, T]): McpServer[F] =
      this.copy(tools = tools :+ tool)

    override def addResource(resource: McpSchema.ResourceHandler[F]): McpServer[F] =
      this.copy(resources = resources :+ resource)

    override def addPrompt(prompt: McpSchema.PromptHandler[F]): McpServer[F] =
      this.copy(prompts = prompts :+ prompt)

    override def setCapabilities(capabilities: McpSchema.ServerCapabilities): McpServer[F] =
      this.copy(capabilities = capabilities)

    override def connect(transport: McpTransport[F]): McpServer[F] =
      this.copy(transport = transport)

    override def start(): F[Unit] =
      transport.handleRequest()

  case class FastMcp[F[_]: Async: LiftIO](
    serverInfo:   McpSchema.Implementation,
    capabilities: McpSchema.ServerCapabilities,
    tools:        List[McpSchema.Tool[F, ?]],
    resources:    List[McpSchema.ResourceHandler[F]],
    prompts:      List[McpSchema.PromptHandler[F]],
    handlers:     Map[Method, RequestHandler[F]]
  ):

    private def handleProvider: RequestHandler.Provider[F] = new RequestHandler.Provider[F](
      serverInfo,
      capabilities,
      tools,
      resources,
      prompts
    )

    def addTool[T](tool: McpSchema.Tool[F, T]): FastMcp[F] =
      this.copy(tools = tools :+ tool)

    def addResource(resource: McpSchema.ResourceHandler[F]): FastMcp[F] =
      this.copy(resources = resources :+ resource)

    def addPrompt(prompt: McpSchema.PromptHandler[F]): FastMcp[F] =
      this.copy(prompts = prompts :+ prompt)

    def setCapabilities(capabilities: McpSchema.ServerCapabilities): FastMcp[F] =
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
      val serverInfo = McpSchema.Implementation(name, version)
      val capabilities = McpSchema.ServerCapabilities(
        PromptCapabilities(false),
        ResourceCapabilities(None, None),
        McpSchema.ToolCapabilities(false)
      )
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
