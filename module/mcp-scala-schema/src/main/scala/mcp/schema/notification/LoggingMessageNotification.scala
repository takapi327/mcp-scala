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
 * Notification of a log message passed from server to client. If no logging/setLevel request has been sent from the client, the server MAY decide which messages to send automatically.
 */
final case class LoggingMessageNotification(level: LoggingLevel, logger: Option[String], data: Json)
  extends Notification:
  override def method: Method = Method.METHOD_NOTIFICATION_MESSAGE

object LoggingMessageNotification:
  given Decoder[LoggingMessageNotification] = Decoder.instance { cursor =>
    for {
      method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_MESSAGE)
      level <- cursor.get[LoggingLevel]("level")
      logger <- cursor.get[Option[String]]("logger")
      data <- cursor.get[Json]("data")
    } yield
      if method then LoggingMessageNotification(level, logger, data)
      else throw new Exception("Invalid method for LoggingMessageNotification")
  }

  given Encoder[LoggingMessageNotification] = Encoder.instance { message =>
    Json.obj(
      "method" -> message.method.asJson,
      "params" -> Json.obj(
        "level" -> message.level.asJson,
        "logger" -> message.logger.asJson,
        "data" -> message.data.asJson
      )
    )
  }
