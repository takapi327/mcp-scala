/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package result

import io.circe.*

import mcp.schema.McpSchema.Prompt

/**
 * The server's response to a prompts/list request from the client.
 */
final case class ListPromptsResult(prompts: List[Prompt], nextCursor: Option[Cursor]) extends PaginatedResult

object ListPromptsResult:
  given Decoder[ListPromptsResult] = Decoder.derived[ListPromptsResult]
  given Encoder[ListPromptsResult] = Encoder.derived[ListPromptsResult].mapJson(_.dropNullValues)
