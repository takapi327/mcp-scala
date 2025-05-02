/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package request

import io.circe.*
import io.circe.syntax.*

final case class CallToolRequest(name: String, arguments: Option[Json]) extends Request:
  override def method: Method = Method.METHOD_TOOLS_CALL

object CallToolRequest:
  given Decoder[CallToolRequest] = Decoder.instance { cursor =>
    for {
      name <- cursor.get[String]("name")
      arguments <- cursor.get[Option[Json]]("arguments")
    } yield CallToolRequest(name, arguments)
  }

  given Encoder[CallToolRequest] = Encoder.instance { call =>
    Json
      .obj(
        "method" -> call.method.asJson,
        "params" -> Json.obj(
          "name" -> call.name.asJson,
          "arguments" -> call.arguments.asJson
        )
      )
      .dropNullValues
  }
