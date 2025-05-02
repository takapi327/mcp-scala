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
 * A request from the client to the server, to enable or adjust logging.
 */
final case class SetLevelRequest(level: LoggingLevel) extends Request:
  override def method: Method = Method.METHOD_LOGGING_SET_LEVEL

object SetLevelRequest:
  given Decoder[SetLevelRequest] = Decoder.instance { cursor =>
    for {
      level <- cursor.get[LoggingLevel]("level")
    } yield SetLevelRequest(level)
  }

  given Encoder[SetLevelRequest] = Encoder.instance { set =>
    Json
      .obj(
        "method" -> set.method.asJson,
        "params" -> Json.obj(
          "level" -> set.level.asJson
        )
      )
      .dropNullValues
  }
