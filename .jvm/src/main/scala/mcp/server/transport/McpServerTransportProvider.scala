/*
package mcp.server.transport

import java.util.Map as JMap

/**
 * Interface for MCP Server Transport Providers, which establish communication
 * channels with clients.
 */
trait McpServerTransportProvider[F[_]]:
  /**
   * Sets the session factory to create new sessions for client connections.
   */
  def setSessionFactory(sessionFactory: McpServerSession.Factory[F]): F[Unit]

  /**
   * Sends a notification to all connected clients.
   */
  def notifyClients(method: String, params: JMap[String, Object]): F[Unit]

  /**
   * Closes all connections gracefully.
   */
  def closeGracefully(): F[Unit]
 */