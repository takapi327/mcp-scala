/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*

final case class PromptCapabilities(listChanged: Boolean)
object PromptCapabilities:
  given Decoder[PromptCapabilities] = Decoder.derived[PromptCapabilities]
  given Encoder[PromptCapabilities] = Encoder.derived[PromptCapabilities]
