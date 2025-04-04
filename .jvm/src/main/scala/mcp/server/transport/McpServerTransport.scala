/*
package mcp.server.transport

/**
 * Interface for transporting messages between clients and servers.
 */
trait McpServerTransport[F[_]]:
  /**
   * Sends a JSON-RPC message to the client.
   */
  def sendMessage(message: JSONRPCMessage): F[Unit]

  /**
   * Unmarshal data from a format to a specific type.
   */
  def unmarshalFrom[T](data: Any, typeReference: TypeReference[T]): T

  /**
   * Closes the transport gracefully.
   */
  def closeGracefully(): F[Unit]

  /**
   * Closes the transport immediately.
   */
  def close(): Unit
 */