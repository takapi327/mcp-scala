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
 * An optional notification from the server to the client, informing it that the list of resources it can read from has changed. This may be issued by servers without any previous subscription from the client.
 */
final case class ResourceListChangedNotification() extends Notification:
  override def method: Method = Method.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED

object ResourceListChangedNotification:
  given Decoder[ResourceListChangedNotification] = Decoder.instance { cursor =>
    cursor.get[Method]("method").map {
      case Method.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED => ResourceListChangedNotification()
      case _ => throw new Exception("Invalid method for ResourceListChangedNotification")
    }
  }

  given Encoder[ResourceListChangedNotification] = Encoder.instance { list =>
    Json.obj(
      "method" -> list.method.asJson
    )
  }
