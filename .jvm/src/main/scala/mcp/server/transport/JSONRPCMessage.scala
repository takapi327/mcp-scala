/*
package mcp.server.transport

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder, Json}

/**
 * Represents a JSON-RPC message in the protocol.
 */
final case class JSONRPCMessage(
  jsonrpc: String = "2.0",
  id: Option[Either[String, Long]] = None,
  method: Option[String] = None,
  params: Option[Json] = None,
  result: Option[Json] = None,
  error: Option[ErrorObject] = None
)

object JSONRPCMessage:
  implicit val decoder: Decoder[JSONRPCMessage] = deriveDecoder
  implicit val encoder: Encoder[JSONRPCMessage] = deriveEncoder

/**
 * Represents an error object in a JSON-RPC message.
 */
final case class ErrorObject(
  code: Int,
  message: String,
  data: Option[Json] = None
)

object ErrorObject:
  implicit val decoder: Decoder[ErrorObject] = deriveDecoder
  implicit val encoder: Encoder[ErrorObject] = deriveEncoder

/**
 * Type reference class as a replacement for Java's TypeReference.
 * This is used for type-safe deserialization.
 */
sealed abstract class TypeReference[T]()(using val decoder: Decoder[T])
 */