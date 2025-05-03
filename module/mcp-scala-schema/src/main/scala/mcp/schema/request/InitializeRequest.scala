/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package request

import io.circe.*
import io.circe.syntax.*

import mcp.schema.McpSchema.{ Implementation }

/**
 * This request is sent from the client to the server when it first connects, asking it to begin initialization.
 */
final case class InitializeRequest(
  protocolVersion: String,
  capabilities:    ClientCapabilities,
  clientInfo:      Implementation
) extends Request:
  override def method: Method = Method.METHOD_INITIALIZE

object InitializeRequest:
  given Decoder[InitializeRequest] = Decoder.instance { cursor =>
    for
      protocolVersion <- cursor.get[String]("protocolVersion")
      capabilities    <- cursor.get[ClientCapabilities]("capabilities")
      clientInfo      <- cursor.get[Implementation]("clientInfo")
    yield InitializeRequest(protocolVersion, capabilities, clientInfo)
  }

  given Encoder[InitializeRequest] = Encoder.instance { init =>
    Json.obj(
      "method" -> init.method.asJson,
      "params" -> Json.obj(
        "protocolVersion" -> init.protocolVersion.asJson,
        "capabilities"    -> init.capabilities.asJson,
        "clientInfo"      -> init.clientInfo.asJson
      )
    )
  }
