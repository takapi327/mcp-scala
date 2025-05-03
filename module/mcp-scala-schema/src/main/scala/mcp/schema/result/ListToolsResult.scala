/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package result

import io.circe.*

/**
 * The server's response to a tools/list request from the client.
 */
final case class ListToolsResult(tools: List[ToolSchema], nextCursor: Option[Cursor]) extends PaginatedResult

object ListToolsResult:
  given Encoder[ListToolsResult] = Encoder.derived[ListToolsResult].mapJson(_.dropNullValues)
