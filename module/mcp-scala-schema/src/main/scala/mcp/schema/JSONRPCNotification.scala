/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*

/**
 * A notification that does not expect a response.
 */
final case class JSONRPCNotification(
  jsonrpc: String,
  method: Method,
  params: Option[Json]
) extends JSONRPCMessage

object JSONRPCNotification:
  given Decoder[JSONRPCNotification] = Decoder.derived[JSONRPCNotification]
  given Encoder[JSONRPCNotification] = Encoder.derived[JSONRPCNotification]
