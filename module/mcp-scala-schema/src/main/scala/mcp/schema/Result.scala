/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

import mcp.schema.result.*
import mcp.schema.McpSchema.{
  Content,
  Root,
  StopReason,
  ToolSchema
}

object Result:

  /**
   * The server's response to a tools/list request from the client.
   */
  final case class ListToolsResult(tools: List[ToolSchema], nextCursor: Option[Cursor]) extends PaginatedResult
  object ListToolsResult:
    given Encoder[ListToolsResult] = Encoder.derived[ListToolsResult].mapJson(_.dropNullValues)

  /**
   * The server's response to a tool call.
   *
   * Any errors that originate from the tool SHOULD be reported inside the result
   * object, with `isError` set to true, _not_ as an MCP protocol-level error
   * response. Otherwise, the LLM would not be able to see that an error occurred
   * and self-correct.
   *
   * However, any errors in _finding_ the tool, an error indicating that the
   * server does not support tool calls, or any other exceptional conditions,
   * should be reported as an MCP error response.
   */
  final case class CallToolResult(
    content: List[Content],
    isError: Option[Boolean]
  ) extends Result
  object CallToolResult:
    given Decoder[CallToolResult] = Decoder.derived[CallToolResult]
    given Encoder[CallToolResult] = Encoder.derived[CallToolResult].mapJson(_.dropNullValues)

  /**
   * The client's response to a sampling/create_message request from the server. The client should inform the user before returning the sampled message, to allow them to inspect the response (human in the loop) and decide whether to allow the server to see it.
   * TODO: extends SamplingMessage
   * @see https://github.com/modelcontextprotocol/modelcontextprotocol/blob/main/schema/2025-03-26/schema.ts#L902-L919
   */
  final case class CreateMessageResult(model: String, stopReason: Option[StopReason]) extends Result
  object CreateMessageResult:
    given Decoder[CreateMessageResult] = Decoder.derived[CreateMessageResult]
    given Encoder[CreateMessageResult] = Encoder.derived[CreateMessageResult]

  /**
   * The server's response to a completion/complete request
   */
  final case class CompleteResult(values: List[String], total: Option[Int], hasMore: Option[Boolean]) extends Result
  object CompleteResult:
    given Decoder[CompleteResult] = Decoder.instance { cursor =>
      for {
        values  <- cursor.get[List[String]]("completion.values")
        total   <- cursor.get[Option[Int]]("completion.total")
        hasMore <- cursor.get[Option[Boolean]]("completion.hasMore")
      } yield CompleteResult(values, total, hasMore)
    }
    given Encoder[CompleteResult] = Encoder.instance { result =>
      Json
        .obj(
          "completion" -> Json.obj(
            "values"  -> result.values.asJson,
            "total"   -> result.total.asJson,
            "hasMore" -> result.hasMore.asJson
          )
        )
        .dropNullValues
    }

  /**
   * The client's response to a roots/list request from the server.
   * This result contains an array of Root objects, each representing a root directory
   * or file that the server can operate on.
   */
  final case class ListRootsResult(roots: List[Root]) extends Result
  object ListRootsResult:
    given Decoder[ListRootsResult] = Decoder.derived[ListRootsResult]
    given Encoder[ListRootsResult] = Encoder.derived[ListRootsResult]
