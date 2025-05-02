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
}

object Result:

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
