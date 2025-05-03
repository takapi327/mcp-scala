/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*

/**
 * Represents a root directory or file that the server can operate on.
 *
 * @param uri  The URI identifying the root. This *must* start with file:// for now.
 *             This restriction may be relaxed in future versions of the protocol to allow other
 *             URI schemes.
 * @param name An optional name for the root. This can be used to provide a
 *             human-readable identifier for the root, which may be useful for display purposes or
 *             for referencing the root in other parts of the application.
 */
final case class Root(
                       uri: String,
                       name: Option[String]
                     )

object Root:
  given Decoder[Root] = Decoder.derived[Root]
  given Encoder[Root] = Encoder.derived[Root]
