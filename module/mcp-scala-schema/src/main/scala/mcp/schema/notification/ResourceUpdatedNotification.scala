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
 * A notification from the server to the client, informing it that a resource has changed and may need to be read again. This should only be sent if the client previously sent a resources/subscribe request.
 */
final case class ResourceUpdatedNotification(uri: String) extends Notification:
  override def method: Method = Method.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED

object ResourceUpdatedNotification:
  given Decoder[ResourceUpdatedNotification] = Decoder.instance { cursor =>
    for {
      method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED)
      uri <- cursor.get[String]("uri")
    } yield
      if method then ResourceUpdatedNotification(uri)
      else throw new Exception("Invalid method for ResourceUpdatedNotification")
  }

  given Encoder[ResourceUpdatedNotification] = Encoder.instance { update =>
    Json.obj(
      "method" -> update.method.asJson,
      "params" -> Json.obj(
        "uri" -> update.uri.asJson
      )
    )
  }
