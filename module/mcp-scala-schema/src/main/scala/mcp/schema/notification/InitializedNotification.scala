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
 * This notification is sent from the client to the server after initialization has finished.
 */
final case class InitializedNotification() extends Notification:
  override def method: Method = Method.METHOD_NOTIFICATION_INITIALIZED

object InitializedNotification:
  given Decoder[InitializedNotification] = Decoder.instance { cursor =>
    cursor.get[Method]("method").map {
      case Method.METHOD_NOTIFICATION_INITIALIZED => InitializedNotification()
      case _ => throw new Exception("Invalid method for InitializedNotification")
    }
  }

  given Encoder[InitializedNotification] = Encoder.instance { init =>
    Json.obj(
      "method" -> init.method.asJson
    )
  }
