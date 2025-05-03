/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*

/**
 * Clients can implement additional features to enrich connected MCP servers with
 * additional capabilities. These capabilities can be used to extend the functionality
 * of the server, or to provide additional information to the server about the
 * client's capabilities.
 *
 * @param experimental WIP
 * @param roots        define the boundaries of where servers can operate within the
 *                     filesystem, allowing them to understand which directories and files they have
 *                     access to.
 * @param sampling     Provides a standardized way for servers to request LLM sampling
 *                     (“completions” or “generations”) from language models via clients.
 *
 */
final case class ClientCapabilities(
                                     experimental: Option[Map[String, Json]],
                                     roots: Option[RootCapabilities],
                                     sampling: Option[Sampling]
                                   )

object ClientCapabilities:
  given Decoder[ClientCapabilities] = Decoder.derived[ClientCapabilities]
  given Encoder[ClientCapabilities] = Encoder.derived[ClientCapabilities]
