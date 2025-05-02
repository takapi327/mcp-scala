/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp

import io.circe.*

/**
 * @see https://github.com/modelcontextprotocol/modelcontextprotocol/blob/main/schema/2025-03-26/schema.ts
 */
package object schema:

  val LATEST_PROTOCOL_VERSION: String = "2024-11-05"
  val JSONRPC_VERSION:         String = "2.0"

  /**
   * A progress token, used to associate progress notifications with the original request.
   */
  enum ProgressToken:
    case StringToken(value: String)
    case NumberToken(value: BigDecimal)
    case NullToken
  object ProgressToken:
    given Decoder[ProgressToken] = Decoder.instance { cursor =>
      cursor.focus match
        case Some(Json.Null) => Right(ProgressToken.NullToken)
        case Some(json) if json.isString =>
          json.asString
            .map(ProgressToken.StringToken.apply)
            .toRight(DecodingFailure("Expected string", cursor.history))
        case Some(json) if json.isNumber =>
          json.asNumber
            .flatMap(_.toBigDecimal)
            .map(ProgressToken.NumberToken.apply)
            .toRight(DecodingFailure("Expected number", cursor.history))
        case _ =>
          Left(
            DecodingFailure("Invalid JSON-RPC progress token", cursor.history)
          )
    }

    given Encoder[ProgressToken] = Encoder.instance {
      case ProgressToken.StringToken(value) => Json.fromString(value)
      case ProgressToken.NumberToken(value) => Json.fromBigDecimal(value)
      case ProgressToken.NullToken          => Json.Null
    }

  /**
   * An opaque token used to represent a cursor for pagination.
   */
  opaque type Cursor = String
  object Cursor:
    def apply(value:   String): Cursor = value
    def unapply(value: Cursor): String = value

    given Decoder[Cursor] = Decoder.decodeString.map(Cursor.apply)

    given Encoder[Cursor] = Encoder.instance { value =>
      Json.fromString(Cursor.unapply(value))
    }

  /**
   * A uniquely identifying ID for a request in JSON-RPC.
   */
  enum RequestId:
    case StringId(value: String)
    case NumberId(value: BigDecimal)
  object RequestId:
    given Decoder[RequestId] = Decoder.instance { cursor =>
      cursor.focus match
        case Some(json) if json.isString =>
          json.asString
            .map(RequestId.StringId.apply)
            .toRight(DecodingFailure("Expected string", cursor.history))
        case Some(json) if json.isNumber =>
          json.asNumber
            .flatMap(_.toBigDecimal)
            .map(RequestId.NumberId.apply)
            .toRight(DecodingFailure("Expected number", cursor.history))
        case _ =>
          Left(
            DecodingFailure("Invalid JSON-RPC request ID", cursor.history)
          )
    }

    given Encoder[RequestId] = Encoder.instance {
      case RequestId.StringId(value) => Json.fromString(value)
      case RequestId.NumberId(value) => Json.fromBigDecimal(value)
    }

  opaque type Method = String
  object Method:
    def apply(value:   String): Method = value
    def unapply(value: Method): String = value

    // 無限再帰を避けるため、直接文字列をデコードするよう修正
    given Decoder[Method] = Decoder.decodeString.map(Method.apply)

    given Encoder[Method] = Encoder.instance { value =>
      Json.fromString(Method.unapply(value))
    }

    // ---------------------------
    // Method Names
    // ---------------------------

    // Lifecycle Methods
    val METHOD_INITIALIZE:               Method = "initialize"
    val METHOD_NOTIFICATION_INITIALIZED: Method = "notifications/initialized"
    val METHOD_PING:                     Method = "ping"

    // Tool Methods
    val METHOD_TOOLS_LIST:                      Method = "tools/list"
    val METHOD_TOOLS_CALL:                      Method = "tools/call"
    val METHOD_NOTIFICATION_TOOLS_LIST_CHANGED: Method = "notifications/tools/list_changed"

    // Resources Methods
    val METHOD_RESOURCES_LIST:                      Method = "resources/list"
    val METHOD_RESOURCES_READ:                      Method = "resources/read"
    val METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED: Method = "notifications/resources/list_changed"
    val METHOD_RESOURCES_TEMPLATES_LIST:            Method = "resources/templates/list"
    val METHOD_RESOURCES_SUBSCRIBE:                 Method = "resources/subscribe"
    val METHOD_RESOURCES_UNSUBSCRIBE:               Method = "resources/unsubscribe"

    // Prompt Methods
    val METHOD_PROMPT_LIST:                       Method = "prompts/list"
    val METHOD_PROMPT_GET:                        Method = "prompts/get"
    val METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED: Method = "notifications/prompts/list_changed"

    // Logging Methods
    val METHOD_LOGGING_SET_LEVEL:    Method = "logging/setLevel"
    val METHOD_NOTIFICATION_MESSAGE: Method = "notifications/message"

    // Roots Methods
    val METHOD_ROOTS_LIST:                      Method = "roots/list"
    val METHOD_NOTIFICATION_ROOTS_LIST_CHANGED: Method = "notifications/roots/list_changed"

    // Sampling Methods
    val METHOD_SAMPLING_CREATE_MESSAGE: Method = "sampling/createMessage"

    // Cancelled Methods
    val METHOD_CANCEL_NOTIFICATIONS: Method = "notifications/cancelled"

    // Progress Methods
    val METHOD_NOTIFICATION_PROGRESS: Method = "notifications/progress"

    // completion Methods
    val METHOD_COMPLETION_COMPLETE: Method = "completion/complete"

  // ---------------------------
  // JSON-RPC Error Codes
  // ---------------------------
  /**
   * Standard error codes used in MCP JSON-RPC responses.
   */
  object ErrorCodes:
    /**
     * Invalid JSON was received by the server.
     */
    val PARSE_ERROR: Int = -32700

    /**
     * The JSON sent is not a valid Request object.
     */
    val INVALID_REQUEST: Int = -32600

    /**
     * The method does not exist / is not available.
     */
    val METHOD_NOT_FOUND: Int = -32601

    /**
     * Invalid method parameter(s).
     */
    val INVALID_PARAMS: Int = -32602

    /**
     * Internal JSON-RPC error.
     */
    val INTERNAL_ERROR: Int = -32603

  /**
   * The severity of a log message.
   *
   * These map to syslog message severities, as specified in RFC-5424:
   * https://datatracker.ietf.org/doc/html/rfc5424#section-6.2.1
   */
  enum LoggingLevel(val code: Int, val name: String):
    case DEBUG     extends LoggingLevel(0, "debug")
    case INFO      extends LoggingLevel(1, "info")
    case NOTICE    extends LoggingLevel(2, "notice")
    case WARNING   extends LoggingLevel(3, "warning")
    case ERROR     extends LoggingLevel(4, "error")
    case CRITICAL  extends LoggingLevel(5, "critical")
    case ALERT     extends LoggingLevel(6, "alert")
    case EMERGENCY extends LoggingLevel(7, "emergency")

  object LoggingLevel:
    given Decoder[LoggingLevel] = Decoder[String].map {
      case "debug"     => LoggingLevel.DEBUG
      case "info"      => LoggingLevel.INFO
      case "notice"    => LoggingLevel.NOTICE
      case "warning"   => LoggingLevel.WARNING
      case "error"     => LoggingLevel.ERROR
      case "critical"  => LoggingLevel.CRITICAL
      case "alert"     => LoggingLevel.ALERT
      case "emergency" => LoggingLevel.EMERGENCY
    }
    given Encoder[LoggingLevel] = Encoder[String].contramap(_.name)
