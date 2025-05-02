/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package request

import io.circe.*
import io.circe.syntax.*

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
