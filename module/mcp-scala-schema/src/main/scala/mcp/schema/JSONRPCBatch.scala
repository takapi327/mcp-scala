/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

/**
 * A JSON-RPC batch, as described in https://www.jsonrpc.org/specification#batch.
 */
final case class JSONRPCBatch(
  requests: List[JSONRPCMessage]
) extends JSONRPCMessage

object JSONRPCBatch:
  given Decoder[JSONRPCBatch] = Decoder.instance { cursor =>
    cursor.as[List[Json]].flatMap { jsons =>
      val decodedRequests = jsons.map(_.as[JSONRPCMessage])
      val allResults = decodedRequests.collect {
        case Right(request) => request
      }

      if allResults.length == decodedRequests.length then Right(JSONRPCBatch(allResults))
      else
        val errors = decodedRequests.collect {
          case Left(error) => error.message
        }
        Left(DecodingFailure(s"Failed to decode some batch requests: ${ errors.mkString(", ") }", cursor.history))
    }
  }

  given Encoder[JSONRPCBatch] = Encoder.instance { batch =>
    Json.arr(batch.requests.map(_.asJson)*)
  }
