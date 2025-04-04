package mcp.spec

class McpError(jsonRpcError: McpSchema.JSONRPCError) extends RuntimeException:
  
  override def getMessage: String = jsonRpcError.message
