/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

import mcp.schema.request.*
import mcp.schema.result.*

/**
 * Based on the <a href="http://www.jsonrpc.org/specification">JSON-RPC 2.0
 * specification</a> and the <a href=
 * "https://github.com/modelcontextprotocol/specification/blob/main/schema/schema.ts">Model
 * Context Protocol Schema</a>.
 *
 * @see https://github.com/modelcontextprotocol/java-sdk/blob/79ec5b5ed1cc1a7abf2edda313a81875bd75ad86/mcp/src/main/java/io/modelcontextprotocol/spec/McpSchema.java
 */
object McpSchema:






  // ---------------------------
  // Resource Interfaces
  // ---------------------------
  /**
   * Base for objects that include optional annotations for the client. The client can
   * use annotations to inform how objects are used or displayed
   */
  sealed trait Annotated:
    def annotations: Annotations

  trait ResourceHandler[F[_]]:

    def resource: McpResource

    def readHandler: ReadResourceRequest => F[ReadResourceResult]



  // ---------------------------
  // Prompt Interfaces
  // ---------------------------
  /**
   * A prompt or prompt template that the server offers.
   *
   * @param name        The name of the prompt or prompt template.
   * @param description An optional description of what this prompt provides.
   * @param arguments   A list of arguments to use for templating the prompt.
   */
  final case class Prompt(
    name:        String,
    description: String,
    arguments:   List[PromptArgument]
  )
  object Prompt:
    given Decoder[Prompt] = Decoder.derived[Prompt]
    given Encoder[Prompt] = Encoder.derived[Prompt]

  case class PromptHandler[F[_]](
    prompt:  Prompt,
    handler: GetPromptRequest => F[GetPromptResult]
  )

  // ---------------------------
  // Content Types
  // ---------------------------
  sealed trait Content:

    def `type`: String

  object Content:
    given Decoder[Content] = Decoder.instance { c =>
      c.get[String]("type").flatMap {
        case "text"     => c.as[TextContent]
        case "image"    => c.as[ImageContent]
        case "resource" => c.as[EmbeddedResource]
        case _          => Left(DecodingFailure("Unknown content type", c.history))
      }
    }

    given Encoder[Content] = Encoder.instance {
      case text: TextContent          => text.asJson
      case image: ImageContent        => image.asJson
      case resource: EmbeddedResource => resource.asJson
    }

    def text(text: String): TextContent = TextContent(None, None, text)

  final case class TextContent(
    audience: Option[List[Role]],
    priority: Option[Double],
    text:     String
  ) extends Content:
    override def `type`: String = "text"

  object TextContent:
    given Decoder[TextContent] = Decoder.derived[TextContent]
    given Encoder[TextContent] = Encoder.instance { txt =>
      Json
        .obj(
          "type"     -> Json.fromString(txt.`type`),
          "audience" -> txt.audience.asJson,
          "priority" -> txt.priority.asJson,
          "text"     -> Json.fromString(txt.text)
        )
        .dropNullValues
    }

  final case class ImageContent(
    audience: List[Role],
    priority: Double,
    data:     String,
    mimeType: String
  ) extends Content:
    override def `type`: String = "image"

  object ImageContent:
    given Decoder[ImageContent] = Decoder.derived[ImageContent]
    given Encoder[ImageContent] = Encoder.derived[ImageContent]

  final case class EmbeddedResource(
    audience: List[Role],
    priority: Double,
    resource: ResourceContents
  ) extends Content:
    override def `type`: String = "resource"

  object EmbeddedResource:
    given Decoder[EmbeddedResource] = Decoder.derived[EmbeddedResource]
    given Encoder[EmbeddedResource] = Encoder.derived[EmbeddedResource]

  /**
   * Describes a message returned as part of a prompt.
   *
   * This is similar to `SamplingMessage`, but also supports the embedding of resources
   * from the MCP server.
   *
   * @param role    The sender or recipient of messages and data in a conversation.
   * @param content The content of the message of type [[Content]].
   */
  final case class PromptMessage(
    role:    Role,
    content: Content
  )
  object PromptMessage:
    given Decoder[PromptMessage] = Decoder.derived[PromptMessage]
    given Encoder[PromptMessage] = Encoder.derived[PromptMessage]

  /**
   * The server's response to a prompts/list request from the client.
   *
   * @param prompts    A list of prompts that the server provides.
   * @param nextCursor An optional cursor for pagination. If present, indicates there
   *                   are more prompts available.
   */
  final case class ListPromptsResult(
    prompts:    List[Prompt],
    nextCursor: Option[String]
  )
  object ListPromptsResult:
    given Decoder[ListPromptsResult] = Decoder.derived[ListPromptsResult]
    given Encoder[ListPromptsResult] = Encoder.derived[ListPromptsResult].mapJson(_.dropNullValues)

  trait ToolSchema:
    def name: String

    def description: String

    def inputSchema: Json

  object ToolSchema:
    case class Static(
      name:        String,
      description: String,
      inputSchema: Json
    ) extends ToolSchema

    given Encoder[ToolSchema] = Encoder.instance { tool =>
      Json.obj(
        "name"        -> Json.fromString(tool.name),
        "description" -> Json.fromString(tool.description),
        "inputSchema" -> tool.inputSchema
      )
    }

  /**
   * Represents a tool that the server provides. Tools enable servers to expose
   * executable functionality to the system. Through these tools, you can interact with
   * external systems, perform computations, and take actions in the real world.
   *
   * @param name        A unique identifier for the tool. This name is used when calling the
   *                    tool.
   * @param description A human-readable description of what the tool does. This can be
   *                    used by clients to improve the LLM's understanding of available tools.
   */
  final case class Tool[F[_], T: JsonSchema: Decoder](
    name:        String,
    description: String,
    execute:     T => F[CallToolResult]
  ) extends ToolSchema:

    override def inputSchema: Json = summon[JsonSchema[T]].asJson

    def decode(arguments: Json): Decoder.Result[T] =
      summon[Decoder[T]].decodeJson(arguments)

  final case class ModelHint(name: String)
  object ModelHint:
    given Decoder[ModelHint] = Decoder.derived[ModelHint]
    given Encoder[ModelHint] = Encoder.derived[ModelHint]

  // ---------------------------
  // Sampling Interfaces
  // ---------------------------
  final case class ModelPreferences(
    hints:                List[ModelHint],
    costPriority:         Double,
    speedPriority:        Double,
    intelligencePriority: Double
  )
  object ModelPreferences:
    given Decoder[ModelPreferences] = Decoder.derived[ModelPreferences]
    given Encoder[ModelPreferences] = Encoder.derived[ModelPreferences]

  final case class SamplingMessage(
    role:    Role,
    content: Content
  )
  object SamplingMessage:
    given Decoder[SamplingMessage] = Decoder.derived[SamplingMessage]
    given Encoder[SamplingMessage] = Encoder.derived[SamplingMessage]

  enum ContextInclusionStrategy:
    case NONE, THIS_SERVER, ALL_SERVERS
  object ContextInclusionStrategy:
    given Decoder[ContextInclusionStrategy] = Decoder[String].map {
      case "none"       => ContextInclusionStrategy.NONE
      case "thisServer" => ContextInclusionStrategy.THIS_SERVER
      case "allServers" => ContextInclusionStrategy.ALL_SERVERS
    }
    given Encoder[ContextInclusionStrategy] = Encoder[String].contramap {
      case ContextInclusionStrategy.NONE        => "none"
      case ContextInclusionStrategy.THIS_SERVER => "thisServer"
      case ContextInclusionStrategy.ALL_SERVERS => "allServers"
    }

  enum StopReason:
    case END_TURN, STOP_SEQUENCE, MAX_TOKENS
  object StopReason:
    given Decoder[StopReason] = Decoder[String].map {
      case "endTurn"      => StopReason.END_TURN
      case "stopSequence" => StopReason.STOP_SEQUENCE
      case "maxTokens"    => StopReason.MAX_TOKENS
    }
    given Encoder[StopReason] = Encoder[String].contramap {
      case StopReason.END_TURN      => "endTurn"
      case StopReason.STOP_SEQUENCE => "stopSequence"
      case StopReason.MAX_TOKENS    => "maxTokens"
    }

  sealed trait PromptOrResourceReference:

    def `type`: String

  object PromptOrResourceReference:
    given Decoder[PromptOrResourceReference] = Decoder.instance { c =>
      c.get[String]("type").flatMap {
        case "prompt"   => c.as[PromptReference]
        case "resource" => c.as[ResourceReference]
        case _          => Left(DecodingFailure("Unknown reference type", c.history))
      }
    }

    given Encoder[PromptOrResourceReference] = Encoder.instance {
      case prompt: PromptReference     => prompt.asJson
      case resource: ResourceReference => resource.asJson
    }

  final case class PromptReference(
    `type`: String,
    uri:    String
  ) extends PromptOrResourceReference
  object PromptReference:
    given Decoder[PromptReference] = Decoder.derived[PromptReference]
    given Encoder[PromptReference] = Encoder.derived[PromptReference]

  final case class ResourceReference(
    `type`: String,
    uri:    String
  ) extends PromptOrResourceReference
  object ResourceReference:
    given Decoder[ResourceReference] = Decoder.derived[ResourceReference]
    given Encoder[ResourceReference] = Encoder.derived[ResourceReference]

  final case class CompleteArgument(
    name:  String,
    value: String
  )
  object CompleteArgument:
    given Decoder[CompleteArgument] = Decoder.derived[CompleteArgument]
    given Encoder[CompleteArgument] = Encoder.derived[CompleteArgument]

  // ---------------------------
  // Roots
  // ---------------------------
