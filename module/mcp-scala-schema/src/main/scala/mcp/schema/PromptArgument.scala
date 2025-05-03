/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*

/**
 * Describes an argument that a prompt can accept.
 *
 * @param name        The name of the argument.
 * @param description A human-readable description of the argument.
 * @param required    Whether this argument must be provided.
 */
final case class PromptArgument(
                                 name: String,
                                 description: String,
                                 required: Boolean
                               )

object PromptArgument:
  given Decoder[PromptArgument] = Decoder.derived[PromptArgument]
  given Encoder[PromptArgument] = Encoder.derived[PromptArgument]
