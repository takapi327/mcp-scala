package mcp.spec

import scala.concurrent.duration.*

import io.circe.Json
import io.circe.syntax.*

import cats.syntax.all.*

import cats.effect.*

import mcp.server.McpAsyncServerExchange

/**
 * Represents a Model Control Protocol (MCP) session on the server side. It manages
 * bidirectional JSON-RPC communication with the client.
 */
trait McpServerSession[F[_]] extends McpSession[F]:

  /**
   * Retrieve the session id.
   *
   * @return session id
   */
  def getId: String

  def generateRequestId: F[String]

  def handle(message: McpSchema.JSONRPCMessage): F[Unit]

object McpServerSession:

  private val STATE_UNINITIALIZED: Int = 0
  private val STATE_INITIALIZING: Int = 1
  private val STATE_INITIALIZED: Int = 2

  trait InitRequestHandler:
    def handle(initializeRequest: McpSchema.InitializeRequest): McpSchema.InitializeResult

  trait RequestHandler[F[_], T]:
    def handle(exchange: McpAsyncServerExchange[F], params: Json): T

  case class Impl(
    id: String,
    transport: McpTransport[IO],
    initRequestHandler: InitRequestHandler,
    requestHandlers: Map[String, RequestHandler[IO, ?]],
    pendingResponses: Ref[IO, Map[String, Deferred[IO, McpSchema.JSONRPCResponse]]],
    requestCounter: Ref[IO, Int],
    state: Ref[IO, Int],
    clientCapabilities: Ref[IO, McpSchema.ClientCapabilities],
    clientInfo: Ref[IO, McpSchema.Implementation],
                       ) extends McpServerSession[IO]:

    override def getId: String = id

    override def generateRequestId: IO[String] = requestCounter
      .updateAndGet { count =>
        if (count == Int.MaxValue) 0 else count + 1
      }
      .map(_.toString)

    override def sendRequest[T](method: String, requestParams: Json): IO[T] =
      for
        requestId <- generateRequestId
        deferred  <- Deferred[IO, McpSchema.JSONRPCResponse]
        _         <- pendingResponses.update(_.updated(requestId, deferred))
        jsonRpcRequest = McpSchema.JSONRPCRequest(McpSchema.JSONRPC_VERSION, method, requestId, requestParams)
        _ <- transport.sendMessage(jsonRpcRequest).handleErrorWith { error =>
          pendingResponses.update(_.removed(requestId)) *> IO.raiseError[Unit](error)
        }
        response <- IO.race(
          deferred.get,
          IO.sleep(10.seconds) *> IO(new Exception("Request timed out after 10 seconds"))
        ).flatMap {
          case Left(jsonRpcResponse) => 
            if (jsonRpcResponse.error.isDefined)
              IO.raiseError[T](McpError(jsonRpcResponse.error.get))
            else
              IO.delay(transport.unmarshalFrom[T](jsonRpcResponse.result))
          case Right(timeout) => IO.raiseError[T](timeout)
        }
      yield response

    override def handle(message: McpSchema.JSONRPCMessage): IO[Unit] = ???

    private def handleIncomingRequest(request: McpSchema.JSONRPCRequest): IO[McpSchema.JSONRPCResponse] =
      if request.method == McpSchema.METHOD_INITIALIZE then
        request.params.as[McpSchema.InitializeRequest] match
          case Right(initializeRequest) =>
            for
              _ <- state.set(STATE_INITIALIZING)
              _ <- clientInfo.set(initializeRequest.clientInfo)
              _ <- clientCapabilities.set(initializeRequest.capabilities)
              _ <- IO.println(s"Client initialize request - Protocol: ${initializeRequest.protocolVersion}, Capabilities: ${initializeRequest.capabilities}, Info:  ${initializeRequest.clientInfo}")
            yield
              val result = initRequestHandler.handle(initializeRequest)
              McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, request.id, Some(result.asJson), None)
          case Left(error) => IO(McpSchema.JSONRPCResponse(
            McpSchema.JSONRPC_VERSION,
            request.id,
            None,
            Some(McpSchema.JSONRPCError(
              McpSchema.ErrorCodes.INTERNAL_ERROR,
              error.getMessage,
              None
            ))
          ))
      else ???

    private def handleIncomingNotification(notification: McpSchema.JSONRPCNotification): IO[Unit] = ???
