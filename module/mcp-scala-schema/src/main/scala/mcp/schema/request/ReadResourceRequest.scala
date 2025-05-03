/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package request

import io.circe.*
import io.circe.syntax.*

final case class ReadResourceRequest(uri: String) extends Request:
  override def method: Method = Method.METHOD_RESOURCES_READ

object ReadResourceRequest:
  given Decoder[ReadResourceRequest] = Decoder.instance { cursor =>
    for {
      uri <- cursor.get[String]("uri")
    } yield ReadResourceRequest(uri)
  }

  given Encoder[ReadResourceRequest] = Encoder.instance { read =>
    Json.obj(
      "method" -> read.method.asJson,
      "params" -> Json.obj(
        "uri" -> read.uri.asJson
      )
    )
  }
