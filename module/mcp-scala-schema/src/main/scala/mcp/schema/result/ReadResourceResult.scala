/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package result

import io.circe.*

/**
 * The server's response to a resources/read request from the client.
 */
final case class ReadResourceResult(contents: List[ResourceContents]) extends Result

object ReadResourceResult:
  given Decoder[ReadResourceResult] = Decoder.derived[ReadResourceResult]
  given Encoder[ReadResourceResult] = Encoder.derived[ReadResourceResult]
