/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package request

import io.circe.*
import io.circe.syntax.*

final case class ListResourceTemplatesRequest() extends Request:
  override def method: Method = Method.METHOD_RESOURCES_TEMPLATES_LIST

object ListResourceTemplatesRequest:
  given Decoder[ListResourceTemplatesRequest] = Decoder.derived[ListResourceTemplatesRequest]

  given Encoder[ListResourceTemplatesRequest] = Encoder.instance { list =>
    Json.obj(
      "method" -> list.method.asJson
    )
  }
