/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package result

import io.circe.*
import io.circe.syntax.*

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
