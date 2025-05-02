/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package result

import io.circe.*

import mcp.schema.McpSchema.Root

/**
 * The client's response to a roots/list request from the server.
 * This result contains an array of Root objects, each representing a root directory
 * or file that the server can operate on.
 */
final case class ListRootsResult(roots: List[Root]) extends Result

object ListRootsResult:
  given Decoder[ListRootsResult] = Decoder.derived[ListRootsResult]
  given Encoder[ListRootsResult] = Encoder.derived[ListRootsResult]
