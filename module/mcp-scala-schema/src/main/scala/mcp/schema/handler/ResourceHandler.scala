/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema.handler

import mcp.schema.McpResource
import mcp.schema.request.ReadResourceRequest
import mcp.schema.result.ReadResourceResult

trait ResourceHandler[F[_]]:

  def resource: McpResource

  def readHandler: ReadResourceRequest => F[ReadResourceResult]

object ResourceHandler:
  
  private case class Impl[F[_]](
                           resource: McpResource,
                            readHandler: ReadResourceRequest => F[ReadResourceResult]
                         ) extends ResourceHandler[F]
  
  def apply[F[_]](
    resource: McpResource,
    readHandler: ReadResourceRequest => F[ReadResourceResult]
  ): ResourceHandler[F] = Impl(resource, readHandler)
