package mcp.server.transport

import scala.concurrent.duration._
import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.{Async, Concurrent, Resource, Sync}
import cats.effect.std.{Queue, Semaphore}
import cats.syntax.all._
import fs2.{Pipe, Stream}
import fs2.concurrent.SignallingRef

import mcp.spec.{McpSchema, McpTransport, McpSession}

/**
 * Implementation of the MCP Stdio transport provider for servers that communicates using
 * standard input/output streams. Messages are exchanged as newline-delimited JSON-RPC
 * messages over stdin/stdout, with errors and debug information sent to stderr.
 */
class StdioServerTransportProvider

/**
 * Implementation of McpServerTransport for the stdio session.
 */
trait StdioMcpSessionTransport[F[_]] extends McpTransport[F]:
  def start(): F[Unit]
  def isStarted: F[Boolean]
  def inboundStream: Stream[F, McpSchema.JSONRPCMessage]
  def outboundStream: Stream[F, McpSchema.JSONRPCMessage]

object StdioMcpSessionTransport:
  
  case class Impl[F[_]: Async: Concurrent](session: McpSession[F]) extends StdioMcpSessionTransport[F]:
    private val isStartedRef = new AtomicBoolean(false)
    
    // Queues for handling inbound and outbound messages
    private val createQueues = for {
      inbound  <- Queue.bounded[F, McpSchema.JSONRPCMessage](100)
      outbound <- Queue.bounded[F, McpSchema.JSONRPCMessage](100)
    } yield (inbound, outbound)
    
    // Signals to indicate readiness
    private val createSignals = for {
      inboundReady  <- SignallingRef[F, Boolean](false)
      outboundReady <- SignallingRef[F, Boolean](false)
    } yield (inboundReady, outboundReady)
    
    // Resources combined
    private val resources = for {
      queues  <- Resource.eval(createQueues)
      signals <- Resource.eval(createSignals)
    } yield (queues, signals)
    
    private val acquire = resources.allocated.map { case ((queues, signals), cleanup) =>
      (queues, signals, cleanup)
    }
    
    override def start(): F[Unit] = Sync[F].delay {
      isStartedRef.compareAndSet(false, true)
    } *> handleIncomingMessages()
    
    override def isStarted: F[Boolean] = Sync[F].delay(isStartedRef.get())
    
    override def inboundStream: Stream[F, McpSchema.JSONRPCMessage] = 
      Stream.eval(acquire).flatMap { case ((inbound, _), (inboundReady, _), cleanup) =>
        Stream.eval(inboundReady.set(true)) >>
        Stream.fromQueueUnterminated(inbound)
          .onFinalize(cleanup)
      }
    
    override def outboundStream: Stream[F, McpSchema.JSONRPCMessage] =
      Stream.eval(acquire).flatMap { case ((_, outbound), (_, outboundReady), cleanup) =>
        Stream.eval(outboundReady.set(true)) >>
        Stream.fromQueueUnterminated(outbound)
          .onFinalize(cleanup)
      }

    override def sendMessage(message: McpSchema.JSONRPCMessage): F[Unit] = 
      acquire.flatMap { case ((_, outbound), (inboundReady, outboundReady), _) =>
        (inboundReady.get, outboundReady.get).mapN { (inReady, outReady) =>
          if (inReady && outReady) {
            outbound.offer(message).handleErrorWith { error =>
              Sync[F].raiseError(new RuntimeException("Failed to enqueue message", error))
            }
          } else {
            Sync[F].raiseError(new RuntimeException("Transport not ready"))
          }
        }.flatten
      }

    /**
     * Handles incoming messages by processing them through the session handler
     * and ensuring proper cleanup when the stream terminates.
     */
    private def handleIncomingMessages(): F[Unit] =
      inboundStream
        .evalMap(message => session.handle(message))
        .handleErrorWith(error => 
          Stream.eval(Sync[F].delay(println(s"Error processing incoming message: ${error.getMessage}")))
        )
        .onFinalize {
          for {
            // Signal outbound handling to complete
            _ <- acquire.flatMap { case ((_, outbound), _, _) => 
                  outbound.tryOfferOne(None).void 
                }
            // Clean up any resources
            _ <- Sync[F].delay(println("Inbound message handling terminated"))
          } yield ()
        }
        .compile
        .drain

    override def close(): F[Unit] = ???
    override def unmarshalFrom[T](data: Any): T = ???
