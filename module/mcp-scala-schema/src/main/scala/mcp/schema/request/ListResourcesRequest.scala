/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package request

import io.circe.*
import io.circe.syntax.*

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
