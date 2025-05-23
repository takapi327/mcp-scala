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

import mcp.schema.*
import mcp.schema.handler.*
import mcp.schema.request.*
import mcp.schema.result.*

import mcp.server.handler.*

trait RequestHandler[F[_]]:

  def handle(request: Json): F[Either[Throwable, Json]]

object RequestHandler:

  class Provider[F[_]: Async](
    serverInfo:   Implementation,
    capabilities: ServerCapabilities,
    tools:        List[Tool[F, ?]],
    resources:    List[ResourceHandler[F]],
    prompts:      List[PromptHandler[F]]
  ):

    def handlers: Map[Method, RequestHandler[F]] =
      Map(
        // Lifecycle Methods
        Method.METHOD_INITIALIZE               -> Initialize[F](serverInfo, capabilities),
        Method.METHOD_NOTIFICATION_INITIALIZED -> NotificationInitialized[F](),
        Method.METHOD_PING                     -> Ping[F](),
        // Tool Methods
        Method.METHOD_TOOLS_LIST -> ListTools[F](serverInfo, capabilities, tools),
        Method.METHOD_TOOLS_CALL -> CallTools[F](serverInfo, capabilities, tools),
        // Resources Methods
        Method.METHOD_RESOURCES_LIST           -> ResourcesList[F](resources),
        Method.METHOD_RESOURCES_READ           -> ResourcesRead[F](resources),
        Method.METHOD_RESOURCES_TEMPLATES_LIST -> ResourceTemplatesList[F](resources),
        // Prompt Methods
        Method.METHOD_PROMPT_LIST -> PromptList[F](prompts),
        Method.METHOD_PROMPT_GET  -> PromptGet[F](prompts),
        // Logging Methods
        Method.METHOD_LOGGING_SET_LEVEL -> Ping[F]()
        // McpSchema.METHOD_NOTIFICATION_MESSAGE -> ???,
        // Roots Methods
        // McpSchema.METHOD_ROOTS_LIST -> ???,
        // McpSchema.METHOD_NOTIFICATION_ROOTS_LIST_CHANGED -> ???,
        // Sampling Methods
        // McpSchema.METHOD_SAMPLING_CREATE_MESSAGE -> ???,
      )

  final case class Initialize[F[_]: Async](
    serverInfo:   Implementation,
    capabilities: ServerCapabilities
  ) extends RequestHandler[F]:

    override def handle(request: Json): F[Either[Throwable, Json]] =
      request.as[InitializeRequest] match
        case Left(error) => Async[F].pure(Left(error))
        case Right(initializeRequest) =>
          val response = InitializeResult(
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
    serverInfo:   Implementation,
    capabilities: ServerCapabilities,
    tools:        List[Tool[F, ?]]
  ) extends RequestHandler[F]:

    override def handle(request: Json): F[Either[Throwable, Json]] =
      val response = ListToolsResult(tools, None)
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
    serverInfo:   Implementation,
    capabilities: ServerCapabilities,
    tools:        List[Tool[F, ?]]
  ) extends RequestHandler[F]:

    override def handle(request: Json): F[Either[Throwable, Json]] =
      request.as[CallToolRequest] match
        case Left(error) => Async[F].pure(Left(error))
        case Right(callToolRequest) =>
          tools.find(_.name == callToolRequest.name) match
            case None => Async[F].pure(Left(McpError(s"Tool not found: ${ callToolRequest.name }")))
            case Some(tool) =>
              callToolRequest.arguments match {
                case Some(arguments) =>
                  tool.decode(arguments) match
                    case Left(error)  => Async[F].pure(Left(error))
                    case Right(value) => tool.execute(value).map(v => Right(v.asJson))
                case None =>
                  Async[F].pure(Left(McpError("No arguments provided")))
              }
