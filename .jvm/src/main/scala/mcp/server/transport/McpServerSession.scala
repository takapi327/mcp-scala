/*
package mcp.server.transport

import java.util.Map as JMap

/**
 * Interface for MCP server session to handle client messages and manage the session lifecycle.
 */
trait McpServerSession[F[_]]:
  /**
   * Handles a received JSON-RPC message.
   */
  def handle(message: JSONRPCMessage): F[Unit]

  /**
   * Sends a notification to the client.
   */
  def sendNotification(method: String, params: JMap[String, Object]): F[Unit]

  /**
   * Closes the session gracefully.
   */
  def closeGracefully(): F[Unit]

  /**
   * Closes the session immediately.
   */
  def close(): Unit

object McpServerSession:
  /**
   * Factory for creating new McpServerSession instances.
   */
  trait Factory[F[_]]:
    /**
     * Creates a new session with the given transport.
     */
    def create(transport: McpServerTransport[F]): McpServerSession[F]
 */