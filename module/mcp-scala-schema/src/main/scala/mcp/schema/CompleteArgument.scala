/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*

final case class CompleteArgument(
  name:  String,
  value: String
)

object CompleteArgument:
  given Decoder[CompleteArgument] = Decoder.derived[CompleteArgument]
  given Encoder[CompleteArgument] = Encoder.derived[CompleteArgument]
