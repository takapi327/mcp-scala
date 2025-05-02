/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

import mcp.schema.McpSchema.{
  ClientCapabilities,
  CompleteArgument,
  ContextInclusionStrategy,
  Implementation,
  ModelPreferences,
  PromptOrResourceReference,
  SamplingMessage
}

trait Request:

  def method: Method

object Request:

  trait PaginatedRequest extends Request:
    def cursor: Option[Cursor]

  /**
   * This request is sent from the client to the server when it first connects, asking it to begin initialization.
   */
  final case class InitializeRequest(
    protocolVersion: String,
    capabilities:    ClientCapabilities,
    clientInfo:      Implementation
  ) extends Request:
    override def method: Method = Method.METHOD_INITIALIZE
  object InitializeRequest:
    given Decoder[InitializeRequest] = Decoder.instance { cursor =>
      for
        protocolVersion <- cursor.get[String]("protocolVersion")
        capabilities    <- cursor.get[ClientCapabilities]("capabilities")
        clientInfo      <- cursor.get[Implementation]("clientInfo")
      yield InitializeRequest(protocolVersion, capabilities, clientInfo)
    }

    given Encoder[InitializeRequest] = Encoder.instance { init =>
      Json.obj(
        "method" -> init.method.asJson,
        "params" -> Json.obj(
          "protocolVersion" -> init.protocolVersion.asJson,
          "capabilities"    -> init.capabilities.asJson,
          "clientInfo"      -> init.clientInfo.asJson
        )
      )
    }

  /**
   * A ping, issued by either the server or the client, to check that the other party is still alive. The receiver must promptly respond, or else may be disconnected.
   */
  final case class PingRequest() extends Request:
    override def method: Method = Method.METHOD_PING
  object PingRequest:
    given Decoder[PingRequest] = Decoder.derived[PingRequest]
    given Encoder[PingRequest] = Encoder.instance { ping =>
      Json.obj(
        "method" -> ping.method.asJson
      )
    }

  final case class ListResourcesRequest(cursor: Option[Cursor]) extends PaginatedRequest:
    override def method: Method = Method.METHOD_RESOURCES_LIST
  object ListResourcesRequest:
    given Decoder[ListResourcesRequest] = Decoder.instance { cursor =>
      for {
        cursor <- cursor.get[Option[Cursor]]("cursor")
      } yield ListResourcesRequest(cursor)
    }

    given Encoder[ListResourcesRequest] = Encoder.instance { list =>
      Json
        .obj(
          "method" -> list.method.asJson,
          "cursor" -> list.cursor.asJson
        )
        .dropNullValues
    }

  final case class ListResourceTemplatesRequest() extends Request:
    override def method: Method = Method.METHOD_RESOURCES_TEMPLATES_LIST
  object ListResourceTemplatesRequest:
    given Decoder[ListResourceTemplatesRequest] = Decoder.derived[ListResourceTemplatesRequest]

    given Encoder[ListResourceTemplatesRequest] = Encoder.instance { list =>
      Json.obj(
        "method" -> list.method.asJson
      )
    }

  final case class ReadResourceRequest(uri: String) extends Request:
    override def method: Method = Method.METHOD_RESOURCES_READ
  object ReadResourceRequest:
    given Decoder[ReadResourceRequest] = Decoder.instance { cursor =>
      for {
        uri <- cursor.get[String]("uri")
      } yield ReadResourceRequest(uri)
    }

    given Encoder[ReadResourceRequest] = Encoder.instance { read =>
      Json.obj(
        "method" -> read.method.asJson,
        "params" -> Json.obj(
          "uri" -> read.uri.asJson
        )
      )
    }

  /**
   * Sent from the client to request resources/updated notifications from the server whenever a particular resource changes.
   */
  final case class SubscribeRequest(uri: String) extends Request:
    override def method: Method = Method.METHOD_RESOURCES_SUBSCRIBE
  object SubscribeRequest:
    given Decoder[SubscribeRequest] = Decoder.instance { cursor =>
      for {
        uri <- cursor.get[String]("uri")
      } yield SubscribeRequest(uri)
    }

    given Encoder[SubscribeRequest] = Encoder.instance { subscribe =>
      Json.obj(
        "method" -> subscribe.method.asJson,
        "params" -> Json.obj(
          "uri" -> subscribe.uri.asJson
        )
      )
    }

  /**
   * Sent from the client to request cancellation of resources/updated notifications from the server. This should follow a previous resources/subscribe request.
   */
  final case class UnsubscribeRequest(uri: String) extends Request:
    override def method: Method = Method.METHOD_RESOURCES_UNSUBSCRIBE
  object UnsubscribeRequest:
    given Decoder[UnsubscribeRequest] = Decoder.instance { cursor =>
      for {
        uri <- cursor.get[String]("uri")
      } yield UnsubscribeRequest(uri)
    }

    given Encoder[UnsubscribeRequest] = Encoder.instance { unsubscribe =>
      Json.obj(
        "method" -> unsubscribe.method.asJson,
        "params" -> Json.obj(
          "uri" -> unsubscribe.uri.asJson
        )
      )
    }

  final case class ListPromptsRequest(cursor: Option[Cursor]) extends PaginatedRequest:
    override def method: Method = Method.METHOD_PROMPT_LIST
  object ListPromptsRequest:
    given Decoder[ListPromptsRequest] = Decoder.instance { cursor =>
      for {
        cursor <- cursor.get[Option[Cursor]]("cursor")
      } yield ListPromptsRequest(cursor)
    }

    given Encoder[ListPromptsRequest] = Encoder.instance { list =>
      Json
        .obj(
          "method" -> list.method.asJson,
          "cursor" -> list.cursor.asJson
        )
        .dropNullValues
    }

  /**
   * Used by the client to get a prompt provided by the server.
   */
  final case class GetPromptRequest(name: String, arguments: Option[Map[String, Json]]) extends Request:
    override def method: Method = Method.METHOD_PROMPT_GET
  object GetPromptRequest:
    given Decoder[GetPromptRequest] = Decoder.instance { cursor =>
      for {
        name      <- cursor.get[String]("name")
        arguments <- cursor.get[Option[Map[String, Json]]]("arguments")
      } yield GetPromptRequest(name, arguments)
    }

    given Encoder[GetPromptRequest] = Encoder.instance { get =>
      Json.obj(
        "method" -> get.method.asJson,
        "params" -> Json.obj(
          "name"      -> get.name.asJson,
          "arguments" -> get.arguments.asJson
        )
      )
    }

  /**
   * Sent from the client to request a list of tools the server has.
   */
  final case class ListToolsRequest(cursor: Option[Cursor]) extends PaginatedRequest:
    override def method: Method = Method.METHOD_TOOLS_LIST
  object ListToolsRequest:
    given Decoder[ListToolsRequest] = Decoder.instance { cursor =>
      for {
        cursor <- cursor.get[Option[Cursor]]("cursor")
      } yield ListToolsRequest(cursor)
    }

    given Encoder[ListToolsRequest] = Encoder.instance { list =>
      Json
        .obj(
          "method" -> list.method.asJson,
          "cursor" -> list.cursor.asJson
        )
        .dropNullValues
    }

  final case class CallToolRequest(name: String, arguments: Option[Json]) extends Request:
    override def method: Method = Method.METHOD_TOOLS_CALL
  object CallToolRequest:
    given Decoder[CallToolRequest] = Decoder.instance { cursor =>
      for {
        name      <- cursor.get[String]("name")
        arguments <- cursor.get[Option[Json]]("arguments")
      } yield CallToolRequest(name, arguments)
    }

    given Encoder[CallToolRequest] = Encoder.instance { call =>
      Json
        .obj(
          "method" -> call.method.asJson,
          "params" -> Json.obj(
            "name"      -> call.name.asJson,
            "arguments" -> call.arguments.asJson
          )
        )
        .dropNullValues
    }

  /**
   * A request from the client to the server, to enable or adjust logging.
   */
  final case class SetLevelRequest(level: LoggingLevel) extends Request:
    override def method: Method = Method.METHOD_LOGGING_SET_LEVEL
  object SetLevelRequest:
    given Decoder[SetLevelRequest] = Decoder.instance { cursor =>
      for {
        level <- cursor.get[LoggingLevel]("level")
      } yield SetLevelRequest(level)
    }

    given Encoder[SetLevelRequest] = Encoder.instance { set =>
      Json
        .obj(
          "method" -> set.method.asJson,
          "params" -> Json.obj(
            "level" -> set.level.asJson
          )
        )
        .dropNullValues
    }

  /**
   * A request from the server to sample an LLM via the client. The client has full discretion over which model to select. The client should also inform the user before beginning sampling, to allow them to inspect the request (human in the loop) and decide whether to approve it.
   */
  final case class CreateMessageRequest(
    messages:         List[SamplingMessage],
    modelPreferences: Option[ModelPreferences],
    systemPrompt:     Option[String],
    includeContext:   Option[ContextInclusionStrategy],
    temperature:      Option[String],
    maxTokens:        Int,
    stopSequences:    Option[Int],
    metaData:         Option[Json]
  ) extends Request:
    override def method: Method = Method.METHOD_NOTIFICATION_MESSAGE
  object CreateMessageRequest:
    given Decoder[CreateMessageRequest] = Decoder.instance { cursor =>
      for {
        messages         <- cursor.get[List[SamplingMessage]]("messages")
        modelPreferences <- cursor.get[Option[ModelPreferences]]("modelPreferences")
        systemPrompt     <- cursor.get[Option[String]]("systemPrompt")
        includeContext   <- cursor.get[Option[ContextInclusionStrategy]]("includeContext")
        temperature      <- cursor.get[Option[String]]("temperature")
        maxTokens        <- cursor.get[Int]("maxTokens")
        stopSequences    <- cursor.get[Option[Int]]("stopSequences")
        metaData         <- cursor.get[Option[Json]]("metaData")
      } yield CreateMessageRequest(
        messages,
        modelPreferences,
        systemPrompt,
        includeContext,
        temperature,
        maxTokens,
        stopSequences,
        metaData
      )
    }

    given Encoder[CreateMessageRequest] = Encoder.instance { create =>
      Json
        .obj(
          "method" -> create.method.asJson,
          "params" -> Json.obj(
            "messages"         -> create.messages.asJson,
            "modelPreferences" -> create.modelPreferences.asJson,
            "systemPrompt"     -> create.systemPrompt.asJson,
            "includeContext"   -> create.includeContext.asJson,
            "temperature"      -> create.temperature.asJson,
            "maxTokens"        -> create.maxTokens.asJson,
            "stopSequences"    -> create.stopSequences.asJson,
            "metaData"         -> create.metaData.asJson
          )
        )
        .dropNullValues
    }

  final case class CompleteRequest(ref: PromptOrResourceReference, argument: CompleteArgument) extends Request:
    override def method: Method = Method.METHOD_COMPLETION_COMPLETE
  object CompleteRequest:
    given Decoder[CompleteRequest] = Decoder.instance { cursor =>
      for {
        ref      <- cursor.get[PromptOrResourceReference]("ref")
        argument <- cursor.get[CompleteArgument]("argument")
      } yield CompleteRequest(ref, argument)
    }

    given Encoder[CompleteRequest] = Encoder.instance { complete =>
      Json
        .obj(
          "method" -> complete.method.asJson,
          "params" -> Json.obj(
            "ref"      -> complete.ref.asJson,
            "argument" -> complete.argument.asJson
          )
        )
        .dropNullValues
    }

  /**
   * Sent from the server to request a list of root URIs from the client. Roots allow
   * servers to ask for specific directories or files to operate on. A common example
   * for roots is providing a set of repositories or directories a server should operate
   * on.
   *
   * This request is typically used when the server needs to understand the file system
   * structure or access specific locations that the client has permission to read from.
   */
  final case class ListRootsRequest() extends Request:
    override def method: Method = Method.METHOD_ROOTS_LIST
  object ListRootsRequest:
    given Decoder[ListRootsRequest] = Decoder.derived[ListRootsRequest]

    given Encoder[ListRootsRequest] = Encoder.instance { list =>
      Json.obj(
        "method" -> list.method.asJson
      )
    }
