/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*

final case class Implementation(
  name:    String,
  version: String
)

object Implementation:
  given Decoder[Implementation] = Decoder.derived[Implementation]
  given Encoder[Implementation] = Encoder.derived[Implementation]
