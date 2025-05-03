/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*

/**
 * Roots define the boundaries of where servers can operate within the filesystem,
 * allowing them to understand which directories and files they have access to.
 * Servers can request the list of roots from supporting clients and
 * receive notifications when that list changes.
 *
 * @param listChanged Whether the client would send notification about roots
 *                    has changed since the last time the server checked.
 */
final case class RootCapabilities(listChanged: Boolean)

object RootCapabilities:
  given Decoder[RootCapabilities] = Decoder.derived[RootCapabilities]
  given Encoder[RootCapabilities] = Encoder.derived[RootCapabilities]
