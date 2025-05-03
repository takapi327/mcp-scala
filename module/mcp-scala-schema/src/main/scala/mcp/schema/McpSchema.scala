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
  case class PromptHandler[F[_]](
    prompt:  Prompt,
    handler: GetPromptRequest => F[GetPromptResult]
  )

  // ---------------------------
  // Content Types
  // ---------------------------



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
