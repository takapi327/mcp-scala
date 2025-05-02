/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*

/**
 * A request that expects a response.
 */
final case class JSONRPCRequest(
  jsonrpc: String,
  method: Method,
  id: RequestId,
  params: Option[Json]
) extends JSONRPCMessage, Request

object JSONRPCRequest:
  given Decoder[JSONRPCRequest] = Decoder.derived[JSONRPCRequest]
  given Encoder[JSONRPCRequest] = Encoder.derived[JSONRPCRequest]
