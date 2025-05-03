/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package request

import io.circe.*
import io.circe.syntax.*

final case class CompleteRequest(ref: PromptOrResourceReference, argument: CompleteArgument) extends Request:
  override def method: Method = Method.METHOD_COMPLETION_COMPLETE

object CompleteRequest:
  given Decoder[CompleteRequest] = Decoder.instance { cursor =>
    for {
      ref      <- cursor.get[PromptOrResourceReference]("ref")
      argument <- cursor.get[CompleteArgument]("argument")
    } yield CompleteRequest(ref, argument)
  }

  given Encoder[CompleteRequest] = Encoder.instance { complete =>
    Json
      .obj(
        "method" -> complete.method.asJson,
        "params" -> Json.obj(
          "ref"      -> complete.ref.asJson,
          "argument" -> complete.argument.asJson
        )
      )
      .dropNullValues
  }
