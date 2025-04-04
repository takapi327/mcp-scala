/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

/**
 * Represents a Model Control Protocol (MCP) error.
 * This class is used to represent errors that occur during an MCP session.
 *
 * @see https://github.com/modelcontextprotocol/java-sdk/blob/79ec5b5ed1cc1a7abf2edda313a81875bd75ad86/mcp/src/main/java/io/modelcontextprotocol/spec/McpError.java
 *
 * @param message
 *   error message
 */
class McpError(message: String) extends RuntimeException:

  override def getMessage: String = message

object McpError:

  def apply(jsonRpcError: McpSchema.JSONRPCError): McpError =
    new McpError(jsonRpcError.message)

  def apply(message: String): McpError =
    new McpError(message)
