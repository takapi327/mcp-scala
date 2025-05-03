/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package notification

import io.circe.*
import io.circe.syntax.*

final case class ToolListChangedNotification() extends Notification:
  override def method: Method = Method.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED

object ToolListChangedNotification:
  given Decoder[ToolListChangedNotification] = Decoder.instance { cursor =>
    cursor.get[Method]("method").map {
      case Method.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED => ToolListChangedNotification()
      case _ => throw new Exception("Invalid method for ToolListChangedNotification")
    }
  }

  given Encoder[ToolListChangedNotification] = Encoder.instance { list =>
    Json.obj(
      "method" -> list.method.asJson
    )
  }
