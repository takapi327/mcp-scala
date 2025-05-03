/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package request

import io.circe.*
import io.circe.syntax.*

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
