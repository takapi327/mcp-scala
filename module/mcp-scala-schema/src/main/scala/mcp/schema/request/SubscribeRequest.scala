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
 * Sent from the client to request resources/updated notifications from the server whenever a particular resource changes.
 */
final case class SubscribeRequest(uri: String) extends Request:
  override def method: Method = Method.METHOD_RESOURCES_SUBSCRIBE

object SubscribeRequest:
  given Decoder[SubscribeRequest] = Decoder.instance { cursor =>
    for {
      uri <- cursor.get[String]("uri")
    } yield SubscribeRequest(uri)
  }

  given Encoder[SubscribeRequest] = Encoder.instance { subscribe =>
    Json.obj(
      "method" -> subscribe.method.asJson,
      "params" -> Json.obj(
        "uri" -> subscribe.uri.asJson
      )
    )
  }
