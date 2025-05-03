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

  /**
   * Provides a standardized way for servers to request LLM
   * sampling ("completions" or "generations") from language
   * models via clients. This flow allows clients to maintain
   * control over model access, selection, and permissions
   * while enabling servers to leverage AI capabilities—with
   * no server API keys necessary. Servers can request text or
   * image-based interactions and optionally include context
   * from MCP servers in their prompts.
   */
  case class Sampling()
  object Sampling:
    given Decoder[Sampling] = Decoder.derived[Sampling]
    given Encoder[Sampling] = Encoder.derived[Sampling]

  enum Role:
    case USER, ASSISTANT
  object Role:
    given Decoder[Role] = Decoder[String].map {
      case "user" => Role.USER
      case "assistant" => Role.ASSISTANT
    }
    given Encoder[Role] = Encoder[String].contramap {
      case Role.USER => "user"
      case Role.ASSISTANT => "assistant"
    }

  /**
   * Optional annotations for the client. The client can use annotations to inform how
   * objects are used or displayed.
   *
   * @param audience Describes who the intended customer of this object or data is. It
   *                 can include multiple entries to indicate content useful for multiple audiences
   *                 (e.g., `["user", "assistant"]`).
   * @param priority Describes how important this data is for operating the server. A
   *                 value of 1 means "most important," and indicates that the data is effectively
   *                 required, while 0 means "least important," and indicates that the data is entirely
   *                 optional. It is a number between 0 and 1.
   */
  final case class Annotations(
                                audience: List[Role],
                                priority: Option[Double]
                              )

  object Annotations:
    given Decoder[Annotations] = Decoder.derived[Annotations]
    given Encoder[Annotations] = Encoder.derived[Annotations].mapJson(_.dropNullValues)

  /**
   * A prompt or prompt template that the server offers.
   *
   * @param name        The name of the prompt or prompt template.
   * @param description An optional description of what this prompt provides.
   * @param arguments   A list of arguments to use for templating the prompt.
   */
  final case class Prompt(
                           name: String,
                           description: String,
                           arguments: List[PromptArgument]
                         )

  object Prompt:
    given Decoder[Prompt] = Decoder.derived[Prompt]
    given Encoder[Prompt] = Encoder.derived[Prompt]

  enum ContextInclusionStrategy:
    case NONE, THIS_SERVER, ALL_SERVERS
  object ContextInclusionStrategy:
    given Decoder[ContextInclusionStrategy] = Decoder[String].map {
      case "none"       => ContextInclusionStrategy.NONE
      case "thisServer" => ContextInclusionStrategy.THIS_SERVER
      case "allServers" => ContextInclusionStrategy.ALL_SERVERS
    }
    given Encoder[ContextInclusionStrategy] = Encoder[String].contramap {
      case ContextInclusionStrategy.NONE        => "none"
      case ContextInclusionStrategy.THIS_SERVER => "thisServer"
      case ContextInclusionStrategy.ALL_SERVERS => "allServers"
    }

  enum StopReason:
    case END_TURN, STOP_SEQUENCE, MAX_TOKENS
  object StopReason:
    given Decoder[StopReason] = Decoder[String].map {
      case "endTurn"      => StopReason.END_TURN
      case "stopSequence" => StopReason.STOP_SEQUENCE
      case "maxTokens"    => StopReason.MAX_TOKENS
    }
    given Encoder[StopReason] = Encoder[String].contramap {
      case StopReason.END_TURN      => "endTurn"
      case StopReason.STOP_SEQUENCE => "stopSequence"
      case StopReason.MAX_TOKENS    => "maxTokens"
    }

  final case class ModelHint(name: String)

  object ModelHint:
    given Decoder[ModelHint] = Decoder.derived[ModelHint]
    given Encoder[ModelHint] = Encoder.derived[ModelHint]

  final case class ModelPreferences(
                                     hints: List[ModelHint],
                                     costPriority: Double,
                                     speedPriority: Double,
                                     intelligencePriority: Double
                                   )

  object ModelPreferences:
    given Decoder[ModelPreferences] = Decoder.derived[ModelPreferences]
    given Encoder[ModelPreferences] = Encoder.derived[ModelPreferences]

  /**
   * Base for objects that include optional annotations for the client. The client can
   * use annotations to inform how objects are used or displayed
   */
  sealed trait Annotated:
    def annotations: Annotations
