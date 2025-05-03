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
 * Sent from the client to request cancellation of resources/updated notifications from the server. This should follow a previous resources/subscribe request.
 */
final case class UnsubscribeRequest(uri: String) extends Request:
  override def method: Method = Method.METHOD_RESOURCES_UNSUBSCRIBE

object UnsubscribeRequest:
  given Decoder[UnsubscribeRequest] = Decoder.instance { cursor =>
    for {
      uri <- cursor.get[String]("uri")
    } yield UnsubscribeRequest(uri)
  }

  given Encoder[UnsubscribeRequest] = Encoder.instance { unsubscribe =>
    Json.obj(
      "method" -> unsubscribe.method.asJson,
      "params" -> Json.obj(
        "uri" -> unsubscribe.uri.asJson
      )
    )
  }
