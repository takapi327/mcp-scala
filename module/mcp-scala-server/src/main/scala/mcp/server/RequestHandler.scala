/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.server

import cats.syntax.all.*

import cats.effect.Async

import io.circe.*
import io.circe.syntax.*

import mcp.schema.{ McpError, McpSchema }

import mcp.server.handler.*

trait RequestHandler[F[_]]:

  def handle(request: Json): F[Either[Throwable, Json]]

object RequestHandler:

  class Provider[F[_]: Async](
    serverInfo:   McpSchema.Implementation,
    capabilities: McpSchema.ServerCapabilities,
    tools:        List[McpSchema.Tool[F, ?]],
    resources:    List[McpSchema.ResourceHandler[F]],
    prompts:      List[McpSchema.PromptHandler[F]]
  ):

    def handlers: Map[McpSchema.Method, RequestHandler[F]] =
      Map(
        // Lifecycle Methods
        McpSchema.METHOD_INITIALIZE               -> Initialize[F](serverInfo, capabilities),
        McpSchema.METHOD_NOTIFICATION_INITIALIZED -> NotificationInitialized[F](),
        McpSchema.METHOD_PING                     -> Ping[F](),
        // Tool Methods
        McpSchema.METHOD_TOOLS_LIST -> ListTools[F](serverInfo, capabilities, tools),
        McpSchema.METHOD_TOOLS_CALL -> CallTools[F](serverInfo, capabilities, tools),
        // Resources Methods
        McpSchema.METHOD_RESOURCES_LIST           -> ResourcesList[F](resources),
        McpSchema.METHOD_RESOURCES_READ           -> ResourcesRead[F](resources),
        McpSchema.METHOD_RESOURCES_TEMPLATES_LIST -> ResourceTemplatesList[F](resources),
        // Prompt Methods
        McpSchema.METHOD_PROMPT_LIST -> PromptList[F](prompts),
        McpSchema.METHOD_PROMPT_GET  -> PromptGet[F](prompts),
        // Logging Methods
        McpSchema.METHOD_LOGGING_SET_LEVEL -> Ping[F]()
        // McpSchema.METHOD_NOTIFICATION_MESSAGE -> ???,
        // Roots Methods
        // McpSchema.METHOD_ROOTS_LIST -> ???,
        // McpSchema.METHOD_NOTIFICATION_ROOTS_LIST_CHANGED -> ???,
        // Sampling Methods
        // McpSchema.METHOD_SAMPLING_CREATE_MESSAGE -> ???,
      )

  final case class Initialize[F[_]: Async](
    serverInfo:   McpSchema.Implementation,
    capabilities: McpSchema.ServerCapabilities
  ) extends RequestHandler[F]:

    override def handle(request: Json): F[Either[Throwable, Json]] =
      request.as[McpSchema.InitializeRequest] match
        case Left(error) => Async[F].pure(Left(error))
        case Right(initializeRequest) =>
          val response = McpSchema.InitializeResult(
            initializeRequest.protocolVersion,
            capabilities,
            serverInfo,
            Some("This server is still under development")
          )
          Async[F].pure(Right(response.asJson))

  final case class Ping[F[_]: Async]() extends RequestHandler[F]:

    override def handle(request: Json): F[Either[Throwable, Json]] =
      Async[F].pure(Right(Json.obj()))

  /**
   * 
   * @see https://github.com/modelcontextprotocol/java-sdk/blob/79ec5b5ed1cc1a7abf2edda313a81875bd75ad86/mcp/src/main/java/io/modelcontextprotocol/server/McpAsyncServer.java#L452
   * 
   * @param serverInfo
   * @param capabilities
   * @param tools
   * @param sync$F$0
   * @tparam F
   */
  final case class ListTools[F[_]: Async](
    serverInfo:   McpSchema.Implementation,
    capabilities: McpSchema.ServerCapabilities,
    tools:        List[McpSchema.Tool[F, ?]]
  ) extends RequestHandler[F]:

    override def handle(request: Json): F[Either[Throwable, Json]] =
      val response = McpSchema.ListToolsResult(tools, None)
      Async[F].pure(Right(response.asJson))

  /**
   * @see https://github.com/modelcontextprotocol/java-sdk/blob/79ec5b5ed1cc1a7abf2edda313a81875bd75ad86/mcp/src/main/java/io/modelcontextprotocol/server/McpAsyncServer.java#L460
   *      
   * @param serverInfo
   * @param capabilities
   * @param tools
   * @param sync$F$0
   * @tparam F
   */
  final case class CallTools[F[_]: Async](
    serverInfo:   McpSchema.Implementation,
    capabilities: McpSchema.ServerCapabilities,
    tools:        List[McpSchema.Tool[F, ?]]
  ) extends RequestHandler[F]:

    override def handle(request: Json): F[Either[Throwable, Json]] =
      request.as[McpSchema.CallToolRequest] match
        case Left(error) => Async[F].pure(Left(error))
        case Right(callToolRequest) =>
          tools.find(_.name == callToolRequest.name) match
            case None => Async[F].pure(Left(McpError(s"Tool not found: ${ callToolRequest.name }")))
            case Some(tool) =>
              tool.decode(callToolRequest.arguments) match
                case Left(error)  => Async[F].pure(Left(error))
                case Right(value) => tool.execute(value).map(v => Right(v.asJson))
