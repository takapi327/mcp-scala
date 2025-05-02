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
 * A ping, issued by either the server or the client, to check that the other party is still alive. The receiver must promptly respond, or else may be disconnected.
 */
final case class PingRequest() extends Request:
  override def method: Method = Method.METHOD_PING

object PingRequest:
  given Decoder[PingRequest] = Decoder.derived[PingRequest]

  given Encoder[PingRequest] = Encoder.instance { ping =>
    Json.obj(
      "method" -> ping.method.asJson
    )
  }
