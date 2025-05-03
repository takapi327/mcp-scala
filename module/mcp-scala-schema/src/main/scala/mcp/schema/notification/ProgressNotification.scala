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
 * An out-of-band notification used to inform the receiver of a progress update for a long-running request.
 */
final case class ProgressNotification(
                                       progressToken: ProgressToken,
                                       progress: Int,
                                       total: Option[Int],
                                       message: Option[String]
                                     ) extends Notification:
  override def method: Method = Method.METHOD_NOTIFICATION_PROGRESS

object ProgressNotification:
  given Decoder[ProgressNotification] = Decoder.instance { cursor =>
    for {
      method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_PROGRESS)
      progressToken <- cursor.get[ProgressToken]("progressToken")
      progress <- cursor.get[Int]("progress")
      total <- cursor.get[Option[Int]]("total")
      message <- cursor.get[Option[String]]("message")
    } yield
      if method then ProgressNotification(progressToken, progress, total, message)
      else throw new Exception("Invalid method for ProgressNotification")
  }

  given Encoder[ProgressNotification] = Encoder.instance { progress =>
    Json.obj(
      "method" -> progress.method.asJson,
      "params" -> Json.obj(
        "progressToken" -> progress.progressToken.asJson,
        "progress" -> progress.progress.asJson,
        "total" -> progress.total.asJson,
        "message" -> progress.message.asJson
      )
    )
  }
