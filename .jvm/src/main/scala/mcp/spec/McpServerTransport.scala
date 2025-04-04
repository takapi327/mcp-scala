package mcp.spec

/**
 * Marker interface for the server-side MCP transport.
 * @tparam F
 *   The effect type used for asynchronous operations.
 */
trait McpServerTransport[F[_]] extends McpTransport[F]
