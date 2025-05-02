/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

import mcp.schema.McpSchema.{
  CompleteArgument,
  PromptOrResourceReference,
}

import mcp.schema.request.*

object Request:

  final case class CompleteRequest(ref: PromptOrResourceReference, argument: CompleteArgument) extends Request:
    override def method: Method = Method.METHOD_COMPLETION_COMPLETE
  object CompleteRequest:
    given Decoder[CompleteRequest] = Decoder.instance { cursor =>
      for {
        ref      <- cursor.get[PromptOrResourceReference]("ref")
        argument <- cursor.get[CompleteArgument]("argument")
      } yield CompleteRequest(ref, argument)
    }

    given Encoder[CompleteRequest] = Encoder.instance { complete =>
      Json
        .obj(
          "method" -> complete.method.asJson,
          "params" -> Json.obj(
            "ref"      -> complete.ref.asJson,
            "argument" -> complete.argument.asJson
          )
        )
        .dropNullValues
    }

  /**
   * Sent from the server to request a list of root URIs from the client. Roots allow
   * servers to ask for specific directories or files to operate on. A common example
   * for roots is providing a set of repositories or directories a server should operate
   * on.
   *
   * This request is typically used when the server needs to understand the file system
   * structure or access specific locations that the client has permission to read from.
   */
  final case class ListRootsRequest() extends Request:
    override def method: Method = Method.METHOD_ROOTS_LIST
  object ListRootsRequest:
    given Decoder[ListRootsRequest] = Decoder.derived[ListRootsRequest]

    given Encoder[ListRootsRequest] = Encoder.instance { list =>
      Json.obj(
        "method" -> list.method.asJson
      )
    }
