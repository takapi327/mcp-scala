/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package result

import io.circe.*

/**
 * The client's response to a sampling/create_message request from the server. The client should inform the user before returning the sampled message, to allow them to inspect the response (human in the loop) and decide whether to allow the server to see it.
 */
final case class CreateMessageResult(
  model:      String,
  stopReason: Option[StopReason],
  role:       Role,
  content:    Content
) extends Result,
          SamplingMessage

object CreateMessageResult:
  given Decoder[CreateMessageResult] = Decoder.derived[CreateMessageResult]
  given Encoder[CreateMessageResult] = Encoder.derived[CreateMessageResult]
