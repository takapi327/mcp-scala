/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package result

import io.circe.*

import mcp.schema.McpSchema.PromptMessage

/**
 * The server's response to a prompts/get request from the client.
 */
final case class GetPromptResult(description: Option[String], messages: List[PromptMessage]) extends Result

object GetPromptResult:
  given Decoder[GetPromptResult] = Decoder.derived[GetPromptResult]
  given Encoder[GetPromptResult] = Encoder.derived[GetPromptResult]
