/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package request

import io.circe.*
import io.circe.syntax.*

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
