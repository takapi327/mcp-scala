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
 * Used by the client to get a prompt provided by the server.
 */
final case class GetPromptRequest(name: String, arguments: Option[Map[String, Json]]) extends Request:
  override def method: Method = Method.METHOD_PROMPT_GET

object GetPromptRequest:
  given Decoder[GetPromptRequest] = Decoder.instance { cursor =>
    for {
      name <- cursor.get[String]("name")
      arguments <- cursor.get[Option[Map[String, Json]]]("arguments")
    } yield GetPromptRequest(name, arguments)
  }

  given Encoder[GetPromptRequest] = Encoder.instance { get =>
    Json.obj(
      "method" -> get.method.asJson,
      "params" -> Json.obj(
        "name" -> get.name.asJson,
        "arguments" -> get.arguments.asJson
      )
    )
  }
