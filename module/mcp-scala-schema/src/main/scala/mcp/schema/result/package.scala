/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

package object result:

  trait Result

  object Result:

    /**
     * A response that indicates success but carries no data.
     */
    final case class Empty() extends Result

  trait PaginatedResult extends Result:
    /**
     * An opaque token representing the pagination position after the last returned result.
     * If present, there may be more results available.
     */
    def nextCursor: Option[Cursor]

  trait SamplingMessage:

    def role: Role

    def content: Content

  object SamplingMessage:

    private case class Impl(role: Role, content: Content) extends SamplingMessage

    def apply(role: Role, content: Content): SamplingMessage = Impl(role, content)

    given Decoder[SamplingMessage] = Decoder.instance { cursor =>
      for {
        role    <- cursor.get[Role]("role")
        content <- cursor.get[Content]("content")
      } yield SamplingMessage(role, content)
    }
    given Encoder[SamplingMessage] = Encoder.instance { message =>
      Json.obj(
        "role"    -> message.role.asJson,
        "content" -> message.content.asJson
      )
    }
