/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import cats.syntax.all.*

import io.circe.*
import io.circe.syntax.*

/**
 * Refers to any valid JSON-RPC object that can be decoded off the wire, or encoded to be sent.
 */
trait JSONRPCMessage

object JSONRPCMessage:
  given Decoder[JSONRPCMessage] = List[Decoder[JSONRPCMessage]](
    Decoder[JSONRPCRequest].widen,
    Decoder[JSONRPCNotification].widen,
    Decoder[JSONRPCBatch].widen,
    Decoder[JSONRPCResponse].widen
  ).reduceLeft(_ or _)

  given Encoder[JSONRPCMessage] = Encoder.instance {
    case request: JSONRPCRequest           => request.asJson
    case notification: JSONRPCNotification => notification.asJson
    case batch: JSONRPCBatch               => batch.asJson
    case response: JSONRPCResponse         => response.asJson
  }
