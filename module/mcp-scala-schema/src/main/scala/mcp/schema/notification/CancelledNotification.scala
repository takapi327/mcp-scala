/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package notification

import io.circe.*
import io.circe.syntax.*

/**
 * This notification can be sent by either side to indicate that it is cancelling a previously-issued request.
 *
 * The request SHOULD still be in-flight, but due to communication latency, it is always possible that this notification MAY arrive after the request has already finished.
 *
 * This notification indicates that the result will be unused, so any associated processing SHOULD cease.
 *
 * A client MUST NOT attempt to cancel its `initialize` request.
 */
final case class CancelledNotification(params: Option[Json]) extends Notification:
  override def method: Method = Method.METHOD_CANCEL_NOTIFICATIONS

object CancelledNotification:
  given Decoder[CancelledNotification] = Decoder.instance { cursor =>
    for {
      method <- cursor.get[Method]("method").map(_ == Method.METHOD_CANCEL_NOTIFICATIONS)
      params <- cursor.get[Option[Json]]("params")
    } yield
      if method then CancelledNotification(params)
      else throw new Exception("Invalid method for CancelledNotification")
  }

  given Encoder[CancelledNotification] = Encoder.instance { cancel =>
    Json
      .obj(
        "method" -> cancel.method.asJson,
        "params" -> cancel.params.asJson
      )
      .dropNullValues
  }
