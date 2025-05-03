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
 * An optional notification from the server to the client, informing it that the list of prompts it offers has changed. This may be issued by servers without any previous subscription from the client.
 */
final case class PromptListChangedNotification() extends Notification:
  override def method: Method = Method.METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED

object PromptListChangedNotification:
  given Decoder[PromptListChangedNotification] = Decoder.instance { cursor =>
    cursor.get[Method]("method").map {
      case Method.METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED => PromptListChangedNotification()
      case _ => throw new Exception("Invalid method for PromptListChangedNotification")
    }
  }

  given Encoder[PromptListChangedNotification] = Encoder.instance { list =>
    Json.obj(
      "method" -> list.method.asJson
    )
  }
