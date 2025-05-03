/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

import mcp.schema.result.*

sealed trait ToolSchema:

  /** A unique identifier for the tool. This name is used when calling the tool. */
  def name: String

  /** A human-readable description of what the tool does. This can be used by clients to
    * improve the LLM's understanding of available tools.
    */
  def description: String

  def inputSchema: Json

object ToolSchema:
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
 */
sealed trait Tool[F[_], T: JsonSchema: Decoder] extends ToolSchema:
  def execute:                 T => F[CallToolResult]
  def decode(arguments: Json): Decoder.Result[T]

object Tool:

  private case class Impl[F[_], T: JsonSchema: Decoder](
    name:        String,
    description: String,
    execute:     T => F[CallToolResult]
  ) extends Tool[F, T]:
    override def inputSchema: Json = summon[JsonSchema[T]].asJson

    override def decode(arguments: Json): Decoder.Result[T] =
      summon[Decoder[T]].decodeJson(arguments)

  def apply[F[_], T: JsonSchema: Decoder](
    name:        String,
    description: String,
    execute:     T => F[CallToolResult]
  ): Tool[F, T] = Impl(name, description, execute)
