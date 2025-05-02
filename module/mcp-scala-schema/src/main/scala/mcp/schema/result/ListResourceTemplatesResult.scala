/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package result

import io.circe.*

import mcp.schema.McpSchema.Resource

final case class ListResourceTemplatesResult(resourceTemplates: List[Resource], nextCursor: Option[Cursor])
  extends PaginatedResult

object ListResourceTemplatesResult:
  given Decoder[ListResourceTemplatesResult] = Decoder.derived[ListResourceTemplatesResult]
  given Encoder[ListResourceTemplatesResult] = Encoder.derived[ListResourceTemplatesResult].mapJson(_.dropNullValues)
