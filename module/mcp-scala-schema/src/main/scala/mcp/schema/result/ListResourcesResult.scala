/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package result

import io.circe.*

final case class ListResourcesResult(resources: List[McpResource], nextCursor: Option[Cursor]) extends PaginatedResult

object ListResourcesResult:
  given Decoder[ListResourcesResult] = Decoder.derived[ListResourcesResult]
  given Encoder[ListResourcesResult] = Encoder.derived[ListResourcesResult].mapJson(_.dropNullValues)
