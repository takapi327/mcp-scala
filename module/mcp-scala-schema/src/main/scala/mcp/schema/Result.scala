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
  Root,
  StopReason
}

object Result:

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
