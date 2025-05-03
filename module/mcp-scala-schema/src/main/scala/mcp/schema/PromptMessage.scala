/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*

/**
 * Describes a message returned as part of a prompt.
 *
 * This is similar to `SamplingMessage`, but also supports the embedding of resources
 * from the MCP server.
 *
 * @param role    The sender or recipient of messages and data in a conversation.
 * @param content The content of the message of type [[Content]].
 */
final case class PromptMessage(
                                role: Role,
                                content: Content
                              )

object PromptMessage:
  given Decoder[PromptMessage] = Decoder.derived[PromptMessage]
  given Encoder[PromptMessage] = Encoder.derived[PromptMessage]
