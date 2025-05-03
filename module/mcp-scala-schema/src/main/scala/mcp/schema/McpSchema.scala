/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import mcp.schema.request.*
import mcp.schema.result.*

/**
 * Based on the <a href="http://www.jsonrpc.org/specification">JSON-RPC 2.0
 * specification</a> and the <a href=
 * "https://github.com/modelcontextprotocol/specification/blob/main/schema/schema.ts">Model
 * Context Protocol Schema</a>.
 *
 * @see https://github.com/modelcontextprotocol/java-sdk/blob/79ec5b5ed1cc1a7abf2edda313a81875bd75ad86/mcp/src/main/java/io/modelcontextprotocol/spec/McpSchema.java
 */
object McpSchema:
  trait ResourceHandler[F[_]]:

    def resource: McpResource

    def readHandler: ReadResourceRequest => F[ReadResourceResult]



  // ---------------------------
  // Prompt Interfaces
  // ---------------------------
  case class PromptHandler[F[_]](
    prompt:  Prompt,
    handler: GetPromptRequest => F[GetPromptResult]
  )
