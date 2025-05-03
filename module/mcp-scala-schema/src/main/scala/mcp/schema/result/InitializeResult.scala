/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package result

import io.circe.*

/**
 * After receiving an initialize request from the client, the server sends this response.
 */
final case class InitializeResult(
  protocolVersion: String,
  capabilities:    ServerCapabilities,
  serverInfo:      Implementation,
  instructions:    Option[String]
) extends Result

object InitializeResult:
  given Decoder[InitializeResult] = Decoder.derived[InitializeResult]
  given Encoder[InitializeResult] = Encoder.derived[InitializeResult]
