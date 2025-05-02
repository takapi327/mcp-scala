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
 * A successful or error response to a request.
 */
trait JSONRPCResponse extends JSONRPCMessage:
  def jsonrpc: String
  def id: RequestId
                            
object JSONRPCResponse:
  given Decoder[JSONRPCResponse] =
    List[Decoder[JSONRPCResponse]](
      Decoder[Success].widen,
      Decoder[Error].widen
    ).reduceLeft(_ or _)

  given Encoder[JSONRPCResponse] = Encoder.instance {
    case error: Error => error.asJson
    case success: Success    => success.asJson
  }
  
  final case class Success(
    jsonrpc: String,
    id: RequestId,
    result: Json,
  ) extends JSONRPCResponse
  object Success:
    given Decoder[Success] = Decoder.derived[Success]
    given Encoder[Success] = Encoder.derived[Success]

  final case class Error(
                                 jsonrpc: String,
                                 id: RequestId,
                                 code: Int,
                                 message: String,
                                 data: Option[Json],
                               ) extends JSONRPCResponse
  object Error:
    given Decoder[Error] = Decoder.instance { cursor =>
      for
        jsonrpc <- cursor.get[String]("jsonrpc")
        id <- cursor.get[RequestId]("id")
        code <- cursor.get[Int]("error.code")
        message <- cursor.get[String]("error.message")
        data <- cursor.get[Option[Json]]("error.data")
      yield Error(
        jsonrpc = jsonrpc,
        id = id,
        code = code,
        message = message,
        data = data,
      )
    }

    given Encoder[Error] = Encoder.instance { error =>
      Json.obj(
        "jsonrpc" -> error.jsonrpc.asJson,
        "id" -> error.id.asJson,
        "error" -> Json.obj(
          "code" -> error.code.asJson,
          "message" -> error.message.asJson,
          "data" -> error.data.asJson,
        ),
      )
    }

  def failure(id: RequestId, code: Int,
              message: String,
              data: Option[Json]): JSONRPCResponse = Error(JSONRPC_VERSION, id, code, message, data)
  def success(id: RequestId, result: Json): JSONRPCResponse = Success(JSONRPC_VERSION, id, result)
