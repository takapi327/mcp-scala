/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

/**
 * Based on the <a href="http://www.jsonrpc.org/specification">JSON-RPC 2.0
 * specification</a> and the <a href=
 * "https://github.com/modelcontextprotocol/specification/blob/main/schema/schema.ts">Model
 * Context Protocol Schema</a>.
 *
 * @see https://github.com/modelcontextprotocol/java-sdk/blob/79ec5b5ed1cc1a7abf2edda313a81875bd75ad86/mcp/src/main/java/io/modelcontextprotocol/spec/McpSchema.java
 */
class McpSchema

object McpSchema:

  val LATEST_PROTOCOL_VERSION: String = "2024-11-05"

  val JSONRPC_VERSION: String = "2.0"

  opaque type Method = String
  object Method:

    def apply(value:   String): Method = value
    def unapply(value: Method): String = value

    extension (value: Method) def asString: String = value

    given Decoder[Method] = Decoder.instance { cursor =>
      cursor.as[String].map(Method.apply)
    }
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

  sealed trait JSONRPCMessage:

    def jsonrpc: String

  object JSONRPCMessage:
    // @see: https://github.com/modelcontextprotocol/java-sdk/blob/79ec5b5ed1cc1a7abf2edda313a81875bd75ad86/mcp/src/main/java/io/modelcontextprotocol/spec/McpSchema.java#L149
    given Decoder[JSONRPCMessage] = Decoder.instance { cursor =>
      for
        methodOpt <- cursor.get[Option[String]]("method")
        idCursor = cursor.downField("id")
        resultOpt <- cursor.get[Option[Json]]("result")
        errorOpt  <- cursor.get[Option[JSONRPCError]]("error")
        decoded <- (methodOpt, idCursor.focus.isDefined) match
                     case (Some(method), true) =>
                       cursor.as[JSONRPCRequest]
                     case (Some(method), false) =>
                       cursor.as[JSONRPCNotification]
                     case _ =>
                       if resultOpt.isDefined || errorOpt.isDefined then cursor.as[JSONRPCResponse]
                       else Left(DecodingFailure("Invalid JSON-RPC message", cursor.history))
      yield decoded
    }

    given Encoder[JSONRPCMessage] = Encoder.instance {
      case request: JSONRPCRequest           => request.asJson
      case notification: JSONRPCNotification => notification.asJson
      case batch: JSONRPCBatch               => batch.asJson
      case response: JSONRPCResponse         => response.asJson
    }

  final case class JSONRPCRequest(
    jsonrpc: String,
    method:  Method,
    id:      JSONRPCRequest.Id,
    params:  Option[Json]
  ) extends JSONRPCMessage

  object JSONRPCRequest:
    enum Id:
      case StringId(value: String)
      case NumberId(value: BigDecimal)
      case NullId
    object Id:
      given Decoder[Id] = Decoder.instance { cursor =>
        cursor.focus match
          case Some(Json.Null) => Right(Id.NullId)
          case Some(json) if json.isString =>
            json.asString
              .map(Id.StringId.apply)
              .toRight(DecodingFailure("Expected string", cursor.history))
          case Some(json) if json.isNumber =>
            json.asNumber
              .flatMap(_.toBigDecimal)
              .map(Id.NumberId.apply)
              .toRight(DecodingFailure("Expected number", cursor.history))
          case _ =>
            Left(
              DecodingFailure("Invalid JSON-RPC request ID", cursor.history)
            )
      }

      given Encoder[Id] = Encoder.instance {
        case Id.StringId(value) => Json.fromString(value)
        case Id.NumberId(value) => Json.fromBigDecimal(value)
        case Id.NullId          => Json.Null
      }

    given Decoder[JSONRPCRequest] = Decoder.derived[JSONRPCRequest]
    given Encoder[JSONRPCRequest] = Encoder.derived[JSONRPCRequest]

  final case class JSONRPCNotification(
    jsonrpc: String,
    method:  Method,
    params:  Option[Json]
  ) extends JSONRPCMessage

  object JSONRPCNotification:
    given Decoder[JSONRPCNotification] = Decoder.derived[JSONRPCNotification]
    given Encoder[JSONRPCNotification] = Encoder.derived[JSONRPCNotification]

  final case class JSONRPCBatch(
    jsonrpc:  String,
    requests: List[JSONRPCMessage]
  ) extends JSONRPCMessage

  object JSONRPCBatch:
    given Decoder[JSONRPCBatch] = Decoder.derived[JSONRPCBatch]
    given Encoder[JSONRPCBatch] = Encoder.derived[JSONRPCBatch]

  final case class JSONRPCError(
    code:    Int,
    message: String,
    data:    Option[Json]
  )

  object JSONRPCError:
    given Decoder[JSONRPCError] = Decoder.derived[JSONRPCError]
    given Encoder[JSONRPCError] = Encoder.derived[JSONRPCError]

  final case class JSONRPCResponse(
    jsonrpc: String,
    id:      JSONRPCRequest.Id,
    result:  Option[Json],
    error:   Option[JSONRPCError]
  ) extends JSONRPCMessage

  object JSONRPCResponse:
    // Encoder/Decoderともにnullは除外
    given Decoder[JSONRPCResponse] = Decoder.derived[JSONRPCResponse]
    given Encoder[JSONRPCResponse] = Encoder.derived[JSONRPCResponse].mapJson(_.dropNullValues)

    def apply(
      id:     JSONRPCRequest.Id,
      result: Option[Json],
      error:  Option[JSONRPCError]
    ): JSONRPCResponse = JSONRPCResponse(JSONRPC_VERSION, id, result, error)

    def failure(id: JSONRPCRequest.Id, error: JSONRPCError): JSONRPCResponse = this.apply(id, None, Some(error))
    def failure(error: JSONRPCError):                    JSONRPCResponse = this.failure(JSONRPCRequest.Id.NullId, error)
    def success(id:    JSONRPCRequest.Id, result: Json): JSONRPCResponse = this.apply(id, Some(result), None)

  sealed trait Request

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

  /**
   * Roots define the boundaries of where servers can operate within the filesystem,
   * allowing them to understand which directories and files they have access to.
   * Servers can request the list of roots from supporting clients and
   * receive notifications when that list changes.
   *
   * @param listChanged Whether the client would send notification about roots
   *                    has changed since the last time the server checked.
   */
  final case class RootCapabilities(listChanged: Boolean)
  object RootCapabilities:
    given Decoder[RootCapabilities] = Decoder.derived[RootCapabilities]
    given Encoder[RootCapabilities] = Encoder.derived[RootCapabilities]

  /**
   * Clients can implement additional features to enrich connected MCP servers with
   * additional capabilities. These capabilities can be used to extend the functionality
   * of the server, or to provide additional information to the server about the
   * client's capabilities.
   *
   * @param experimental WIP
   * @param roots        define the boundaries of where servers can operate within the
   *                     filesystem, allowing them to understand which directories and files they have
   *                     access to.
   * @param sampling     Provides a standardized way for servers to request LLM sampling
   *                     (“completions” or “generations”) from language models via clients.
   *
   */
  final case class ClientCapabilities(
    experimental: Option[Map[String, Json]],
    roots:        Option[RootCapabilities],
    sampling:     Option[Sampling]
  )

  object ClientCapabilities:
    given Decoder[ClientCapabilities] = Decoder.derived[ClientCapabilities]
    given Encoder[ClientCapabilities] = Encoder.derived[ClientCapabilities]

  final case class LoggingCapabilities()
  object LoggingCapabilities:
    given Decoder[LoggingCapabilities] = Decoder.derived[LoggingCapabilities]
    given Encoder[LoggingCapabilities] = Encoder.derived[LoggingCapabilities]

  final case class PromptCapabilities(listChanged: Boolean)
  object PromptCapabilities:
    given Decoder[PromptCapabilities] = Decoder.derived[PromptCapabilities]
    given Encoder[PromptCapabilities] = Encoder.derived[PromptCapabilities]

  final case class ResourceCapabilities(
    subscribe:   Option[Boolean],
    listChanged: Option[Boolean]
  )
  object ResourceCapabilities:
    given Decoder[ResourceCapabilities] = Decoder.derived[ResourceCapabilities]
    given Encoder[ResourceCapabilities] = Encoder.derived[ResourceCapabilities].mapJson(_.dropNullValues)

  final case class ToolCapabilities(listChanged: Boolean)
  object ToolCapabilities:
    given Decoder[ToolCapabilities] = Decoder.derived[ToolCapabilities]
    given Encoder[ToolCapabilities] = Encoder.derived[ToolCapabilities]

  final case class ServerCapabilities(
    // experimental: Option[Map[String, Json]],
    // logging: LoggingCapabilities,
    prompt:    PromptCapabilities,
    resources: ResourceCapabilities,
    tools:     ToolCapabilities
  )
  object ServerCapabilities:
    given Decoder[ServerCapabilities] = Decoder.derived[ServerCapabilities]
    given Encoder[ServerCapabilities] = Encoder.derived[ServerCapabilities]

  final case class Implementation(
    name:    String,
    version: String
  )
  object Implementation:
    given Decoder[Implementation] = Decoder.derived[Implementation]
    given Encoder[Implementation] = Encoder.derived[Implementation]

  enum Role:
    case USER, ASSISTANT
  object Role:
    given Decoder[Role] = Decoder[String].map {
      case "user"      => Role.USER
      case "assistant" => Role.ASSISTANT
    }
    given Encoder[Role] = Encoder[String].contramap {
      case Role.USER      => "user"
      case Role.ASSISTANT => "assistant"
    }

  // ---------------------------
  // Initialization
  // ---------------------------
  final case class InitializeRequest(
    protocolVersion: String,
    capabilities:    ClientCapabilities,
    clientInfo:      Implementation
  ) extends Request
  object InitializeRequest:
    given Decoder[InitializeRequest] = Decoder.derived[InitializeRequest]
    given Encoder[InitializeRequest] = Encoder.derived[InitializeRequest]

  final case class InitializeResult(
    protocolVersion: String,
    capabilities:    ServerCapabilities,
    serverInfo:      Implementation,
    instructions:    Option[String]
  )
  object InitializeResult:
    given Decoder[InitializeResult] = Decoder.derived[InitializeResult]
    given Encoder[InitializeResult] = Encoder.derived[InitializeResult]

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

  // ---------------------------
  // Resource Interfaces
  // ---------------------------
  /**
   * Base for objects that include optional annotations for the client. The client can
   * use annotations to inform how objects are used or displayed
   */
  sealed trait Annotated:
    def annotations: Annotations

  trait Resource:
    def name:        String
    def description: Option[String]
    def mimeType:    Option[String]

    private[mcp] def isStatic: Boolean

  object Resource:

    given Decoder[Resource] = Decoder.instance { cursor =>
      cursor.get[Option[String]]("uri").flatMap {
        case Some(uri) =>
          cursor.as[StaticResource]
        case None =>
          cursor.get[Option[String]]("uriTemplate").flatMap {
            case Some(uriTemplate) =>
              cursor.as[ResourceTemplate]
            case None =>
              Left(DecodingFailure("Invalid resource", cursor.history))
          }
      }
    }

    given Encoder[Resource] = Encoder.instance {
      case static: StaticResource     => static.asJson
      case template: ResourceTemplate => template.asJson
    }

    def apply(
      uri:         String,
      name:        String,
      description: Option[String],
      mimeType:    Option[String],
      annotations: Annotations
    ): StaticResource = StaticResource(uri, name, description, mimeType, annotations)

  /**
   * A known resource that the server is capable of reading.
   *
   * @param uri         the URI of the resource.
   * @param name        A human-readable name for this resource. This can be used by clients to
   *                    populate UI elements.
   * @param description A description of what this resource represents. This can be used
   *                    by clients to improve the LLM's understanding of available resources. It can be
   *                    thought of like a "hint" to the model.
   * @param mimeType    The MIME type of this resource, if known.
   * @param annotations Optional annotations for the client. The client can use
   *                    annotations to inform how objects are used or displayed.
   */
  final case class StaticResource(
    uri:         String,
    name:        String,
    description: Option[String],
    mimeType:    Option[String],
    annotations: Annotations
  ) extends Resource:

    override private[mcp] def isStatic: Boolean = true

  object StaticResource:
    given Decoder[StaticResource] = Decoder.derived[StaticResource]
    given Encoder[StaticResource] = Encoder.derived[StaticResource].mapJson(_.dropNullValues)

  trait ResourceHandler[F[_]]:

    def resource: Resource

    def readHandler: ReadResourceRequest => F[ReadResourceResult]

  /**
   * Resource templates allow servers to expose parameterized resources using URI
   * templates.
   *
   * @param uriTemplate A URI template that can be used to generate URIs for this
   *                    resource.
   * @param name        A human-readable name for this resource. This can be used by clients to
   *                    populate UI elements.
   * @param description A description of what this resource represents. This can be used
   *                    by clients to improve the LLM's understanding of available resources. It can be
   *                    thought of like a "hint" to the model.
   * @param mimeType    The MIME type of this resource, if known.
   * @param annotations Optional annotations for the client. The client can use
   *                    annotations to inform how objects are used or displayed.
   * @see <a href="https://datatracker.ietf.org/doc/html/rfc6570">RFC 6570</a>
   */
  final case class ResourceTemplate(
    uriTemplate: String,
    name:        String,
    description: Option[String],
    mimeType:    Option[String],
    annotations: Annotations
  ) extends Resource:

    override private[mcp] def isStatic: Boolean = false

  object ResourceTemplate:
    given Decoder[ResourceTemplate] = Decoder.derived[ResourceTemplate]
    given Encoder[ResourceTemplate] = Encoder.derived[ResourceTemplate].mapJson(_.dropNullValues)

  final case class ListResourcesResult(
    resources:  List[Resource],
    nextCursor: Option[String]
  )
  object ListResourcesResult:
    given Decoder[ListResourcesResult] = Decoder.derived[ListResourcesResult]
    given Encoder[ListResourcesResult] = Encoder.derived[ListResourcesResult].mapJson(_.dropNullValues)

  final case class ListResourceTemplatesResult(
    resourceTemplates: List[Resource],
    nextCursor:        Option[String]
  )
  object ListResourceTemplatesResult:
    given Decoder[ListResourceTemplatesResult] = Decoder.derived[ListResourceTemplatesResult]
    given Encoder[ListResourceTemplatesResult] = Encoder.derived[ListResourceTemplatesResult].mapJson(_.dropNullValues)

  final case class ReadResourceRequest(uri: String)
  object ReadResourceRequest:
    given Decoder[ReadResourceRequest] = Decoder.derived[ReadResourceRequest]
    given Encoder[ReadResourceRequest] = Encoder.derived[ReadResourceRequest]

  sealed trait ResourceContents:

    /**
     * The URI of this resource.
     *
     * @return the URI of this resource.
     */
    def uri: String

    /**
     * The MIME type of this resource.
     *
     * @return the MIME type of this resource.
     */
    def mimeType: String

  object ResourceContents:
    given Decoder[ResourceContents] = Decoder.instance { c =>
      c.get[String]("type").flatMap {
        case "text" => c.as[TextResourceContents]
        case "blob" => c.as[BlobResourceContents]
        case _      => Left(DecodingFailure("Unknown resource type", c.history))
      }
    }

    given Encoder[ResourceContents] = Encoder.instance {
      case text: TextResourceContents => text.asJson
      case blob: BlobResourceContents => blob.asJson
    }

  /**
   * Text contents of a resource.
   *
   * @param uri      the URI of this resource.
   * @param mimeType the MIME type of this resource.
   * @param text     the text of the resource. This must only be set if the resource can
   *                 actually be represented as text (not binary data).
   */
  final case class TextResourceContents(
    uri:      String,
    mimeType: String,
    text:     String
  ) extends ResourceContents
  object TextResourceContents:
    given Decoder[TextResourceContents] = Decoder.derived[TextResourceContents]
    given Encoder[TextResourceContents] = Encoder.derived[TextResourceContents]

  /**
   * Binary contents of a resource.
   *
   * @param uri      the URI of this resource.
   * @param mimeType the MIME type of this resource.
   * @param blob     a base64-encoded string representing the binary data of the resource.
   *                 This must only be set if the resource can actually be represented as binary data
   *                 (not text).
   */
  final case class BlobResourceContents(
    uri:      String,
    mimeType: String,
    blob:     String
  ) extends ResourceContents
  object BlobResourceContents:
    given Decoder[BlobResourceContents] = Decoder.derived[BlobResourceContents]
    given Encoder[BlobResourceContents] = Encoder.derived[BlobResourceContents]

  final case class ReadResourceResult(contents: List[ResourceContents])
  object ReadResourceResult:
    given Decoder[ReadResourceResult] = Decoder.derived[ReadResourceResult]
    given Encoder[ReadResourceResult] = Encoder.derived[ReadResourceResult]

  /**
   * Sent from the client to request resources/updated notifications from the server
   * whenever a particular resource changes.
   *
   * @param uri the URI of the resource to subscribe to. The URI can use any protocol;
   *            it is up to the server how to interpret it.
   */
  final case class SubscribeRequest(uri: String)
  object SubscribeRequest:
    given Decoder[SubscribeRequest] = Decoder.derived[SubscribeRequest]
    given Encoder[SubscribeRequest] = Encoder.derived[SubscribeRequest]

  final case class UnsubscribeRequest(uri: String)
  object UnsubscribeRequest:
    given Decoder[UnsubscribeRequest] = Decoder.derived[UnsubscribeRequest]
    given Encoder[UnsubscribeRequest] = Encoder.derived[UnsubscribeRequest]

  /**
   * Describes an argument that a prompt can accept.
   *
   * @param name        The name of the argument.
   * @param description A human-readable description of the argument.
   * @param required    Whether this argument must be provided.
   */
  final case class PromptArgument(
    name:        String,
    description: String,
    required:    Boolean
  )
  object PromptArgument:
    given Decoder[PromptArgument] = Decoder.derived[PromptArgument]
    given Encoder[PromptArgument] = Encoder.derived[PromptArgument]

  // ---------------------------
  // Prompt Interfaces
  // ---------------------------
  /**
   * A prompt or prompt template that the server offers.
   *
   * @param name        The name of the prompt or prompt template.
   * @param description An optional description of what this prompt provides.
   * @param arguments   A list of arguments to use for templating the prompt.
   */
  final case class Prompt(
    name:        String,
    description: String,
    arguments:   List[PromptArgument]
  )
  object Prompt:
    given Decoder[Prompt] = Decoder.derived[Prompt]
    given Encoder[Prompt] = Encoder.derived[Prompt]

  case class PromptHandler[F[_]](
    prompt:  Prompt,
    handler: GetPromptRequest => F[GetPromptResult]
  )

  // ---------------------------
  // Content Types
  // ---------------------------
  sealed trait Content:

    def `type`: String

  object Content:
    given Decoder[Content] = Decoder.instance { c =>
      c.get[String]("type").flatMap {
        case "text"     => c.as[TextContent]
        case "image"    => c.as[ImageContent]
        case "resource" => c.as[EmbeddedResource]
        case _          => Left(DecodingFailure("Unknown content type", c.history))
      }
    }

    given Encoder[Content] = Encoder.instance {
      case text: TextContent          => text.asJson
      case image: ImageContent        => image.asJson
      case resource: EmbeddedResource => resource.asJson
    }

    def text(text: String): TextContent = TextContent(None, None, text)

  final case class TextContent(
    audience: Option[List[Role]],
    priority: Option[Double],
    text:     String
  ) extends Content:
    override def `type`: String = "text"

  object TextContent:
    given Decoder[TextContent] = Decoder.derived[TextContent]
    given Encoder[TextContent] = Encoder.instance { txt =>
      Json
        .obj(
          "type"     -> Json.fromString(txt.`type`),
          "audience" -> txt.audience.asJson,
          "priority" -> txt.priority.asJson,
          "text"     -> Json.fromString(txt.text)
        )
        .dropNullValues
    }

  final case class ImageContent(
    audience: List[Role],
    priority: Double,
    data:     String,
    mimeType: String
  ) extends Content:
    override def `type`: String = "image"

  object ImageContent:
    given Decoder[ImageContent] = Decoder.derived[ImageContent]
    given Encoder[ImageContent] = Encoder.derived[ImageContent]

  final case class EmbeddedResource(
    audience: List[Role],
    priority: Double,
    resource: ResourceContents
  ) extends Content:
    override def `type`: String = "resource"

  object EmbeddedResource:
    given Decoder[EmbeddedResource] = Decoder.derived[EmbeddedResource]
    given Encoder[EmbeddedResource] = Encoder.derived[EmbeddedResource]

  /**
   * Describes a message returned as part of a prompt.
   *
   * This is similar to `SamplingMessage`, but also supports the embedding of resources
   * from the MCP server.
   *
   * @param role    The sender or recipient of messages and data in a conversation.
   * @param content The content of the message of type [[Content]].
   */
  final case class PromptMessage(
    role:    Role,
    content: Content
  )
  object PromptMessage:
    given Decoder[PromptMessage] = Decoder.derived[PromptMessage]
    given Encoder[PromptMessage] = Encoder.derived[PromptMessage]

  /**
   * The server's response to a prompts/list request from the client.
   *
   * @param prompts    A list of prompts that the server provides.
   * @param nextCursor An optional cursor for pagination. If present, indicates there
   *                   are more prompts available.
   */
  final case class ListPromptsResult(
    prompts:    List[Prompt],
    nextCursor: Option[String]
  )
  object ListPromptsResult:
    given Decoder[ListPromptsResult] = Decoder.derived[ListPromptsResult]
    given Encoder[ListPromptsResult] = Encoder.derived[ListPromptsResult].mapJson(_.dropNullValues)

  /**
   * Used by the client to get a prompt provided by the server.
   *
   * @param name      The name of the prompt or prompt template.
   * @param arguments Arguments to use for templating the prompt.
   */
  final case class GetPromptRequest(
    name:      String,
    arguments: Map[String, Json]
  ) extends Request
  object GetPromptRequest:
    given Decoder[GetPromptRequest] = Decoder.derived[GetPromptRequest]
    given Encoder[GetPromptRequest] = Encoder.derived[GetPromptRequest]

  /**
   * The server's response to a prompts/get request from the client.
   *
   * @param description An optional description for the prompt.
   * @param messages    A list of messages to display as part of the prompt.
   */
  final case class GetPromptResult(
    description: Option[String],
    messages:    List[PromptMessage]
  )
  object GetPromptResult:
    given Decoder[GetPromptResult] = Decoder.derived[GetPromptResult]
    given Encoder[GetPromptResult] = Encoder.derived[GetPromptResult].mapJson(_.dropNullValues)

  trait ToolSchema:
    def name: String

    def description: String

    def inputSchema: Json

  object ToolSchema:
    case class Static(
      name:        String,
      description: String,
      inputSchema: Json
    ) extends ToolSchema

    given Encoder[ToolSchema] = Encoder.instance { tool =>
      Json.obj(
        "name"        -> Json.fromString(tool.name),
        "description" -> Json.fromString(tool.description),
        "inputSchema" -> tool.inputSchema
      )
    }

  /**
   * Represents a tool that the server provides. Tools enable servers to expose
   * executable functionality to the system. Through these tools, you can interact with
   * external systems, perform computations, and take actions in the real world.
   *
   * @param name        A unique identifier for the tool. This name is used when calling the
   *                    tool.
   * @param description A human-readable description of what the tool does. This can be
   *                    used by clients to improve the LLM's understanding of available tools.
   */
  final case class Tool[F[_], T: JsonSchema: Decoder](
    name:        String,
    description: String,
    execute:     T => F[CallToolResult]
  ) extends ToolSchema:

    override def inputSchema: Json = summon[JsonSchema[T]].asJson

    def decode(arguments: Json): Decoder.Result[T] =
      summon[Decoder[T]].decodeJson(arguments)

  // ---------------------------
  // Tool Interfaces
  // ---------------------------
  /**
   * The server's response to a tools/list request from the client.
   *
   * @param tools      A list of tools that the server provides.
   * @param nextCursor An optional cursor for pagination. If present, indicates there
   *                   are more tools available.
   */
  final case class ListToolsResult(
    tools:      List[ToolSchema],
    nextCursor: Option[String]
  )
  object ListToolsResult:
    given Encoder[ListToolsResult] = Encoder.derived[ListToolsResult].mapJson(_.dropNullValues)

  /**
   * Used by the client to call a tool provided by the server.
   *
   * @param name      The name of the tool to call. This must match a tool name from
   *                  tools/list.
   * @param arguments Arguments to pass to the tool. These must conform to the tool's
   *                  input schema.
   */
  final case class CallToolRequest(
    name:      String,
    arguments: Json
  ) extends Request
  object CallToolRequest:
    given Decoder[CallToolRequest] = Decoder.derived[CallToolRequest]
    given Encoder[CallToolRequest] = Encoder.derived[CallToolRequest]

  /**
   * The server's response to a tools/call request from the client.
   *
   * @param content A list of content items representing the tool's output. Each item can be text, an image,
   *                or an embedded resource.
   * @param isError If true, indicates that the tool execution failed and the content contains error information.
   *                If false or absent, indicates successful execution.
   */
  final case class CallToolResult(
    content: List[Content],
    isError: Boolean
  )
  object CallToolResult:
    given Decoder[CallToolResult] = Decoder.derived[CallToolResult]
    given Encoder[CallToolResult] = Encoder.derived[CallToolResult]

    def success(content: List[Content]): CallToolResult =
      CallToolResult(content, false)
    def failure(content: List[Content]): CallToolResult =
      CallToolResult(content, true)

  final case class ModelHint(name: String)
  object ModelHint:
    given Decoder[ModelHint] = Decoder.derived[ModelHint]
    given Encoder[ModelHint] = Encoder.derived[ModelHint]

  // ---------------------------
  // Sampling Interfaces
  // ---------------------------
  final case class ModelPreferences(
    hints:                List[ModelHint],
    costPriority:         Double,
    speedPriority:        Double,
    intelligencePriority: Double
  )
  object ModelPreferences:
    given Decoder[ModelPreferences] = Decoder.derived[ModelPreferences]
    given Encoder[ModelPreferences] = Encoder.derived[ModelPreferences]

  final case class SamplingMessage(
    role:    Role,
    content: Content
  )
  object SamplingMessage:
    given Decoder[SamplingMessage] = Decoder.derived[SamplingMessage]
    given Encoder[SamplingMessage] = Encoder.derived[SamplingMessage]

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

  final case class CreateMessageRequest(
    messages:         List[SamplingMessage],
    modelPreferences: ModelPreferences,
    systemPrompt:     Option[String],
    includeContext:   ContextInclusionStrategy,
    temperature:      Double,
    maxTokens:        Int,
    stopSequences:    List[String],
    metadata:         Map[String, Json]
  ) extends Request
  object CreateMessageRequest:
    given Decoder[CreateMessageRequest] = Decoder.derived[CreateMessageRequest]
    given Encoder[CreateMessageRequest] = Encoder.derived[CreateMessageRequest]

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

  final case class CreateMessageResult(
    role:       Role,
    content:    Content,
    model:      String,
    stopReason: StopReason
  )
  object CreateMessageResult:
    given Decoder[CreateMessageResult] = Decoder.derived[CreateMessageResult]
    given Encoder[CreateMessageResult] = Encoder.derived[CreateMessageResult]

  final case class PaginatedRequest(cursor: String)
  object PaginatedRequest:
    given Decoder[PaginatedRequest] = Decoder.derived[PaginatedRequest]
    given Encoder[PaginatedRequest] = Encoder.derived[PaginatedRequest]

  final case class PaginatedResult(nextCursor: String)
  object PaginatedResult:
    given Decoder[PaginatedResult] = Decoder.derived[PaginatedResult]
    given Encoder[PaginatedResult] = Encoder.derived[PaginatedResult]

  // ---------------------------
  // Progress and Logging
  // ---------------------------
  final case class ProgressNotification(
    progressToken: String,
    progress:      Double,
    total:         Double
  )
  object ProgressNotification:
    given Decoder[ProgressNotification] = Decoder.derived[ProgressNotification]
    given Encoder[ProgressNotification] = Encoder.derived[ProgressNotification]

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
   * The Model Context Protocol (MCP) provides a standardized way for servers to send
   * structured log messages to clients. Clients can control logging verbosity by
   * setting minimum log levels, with servers sending notifications containing severity
   * levels, optional logger names, and arbitrary JSON-serializable data.
   *
   * @param level  The severity levels. The mimimum log level is set by the client.
   * @param logger The logger that generated the message.
   * @param data   JSON-serializable logging data.
   */
  final case class LoggingMessageNotification(
    level:  LoggingLevel,
    logger: String,
    data:   String
  )
  object LoggingMessageNotification:
    given Decoder[LoggingMessageNotification] = Decoder.derived[LoggingMessageNotification]
    given Encoder[LoggingMessageNotification] = Encoder.derived[LoggingMessageNotification]

  sealed trait PromptOrResourceReference:

    def `type`: String

  object PromptOrResourceReference:
    given Decoder[PromptOrResourceReference] = Decoder.instance { c =>
      c.get[String]("type").flatMap {
        case "prompt"   => c.as[PromptReference]
        case "resource" => c.as[ResourceReference]
        case _          => Left(DecodingFailure("Unknown reference type", c.history))
      }
    }

    given Encoder[PromptOrResourceReference] = Encoder.instance {
      case prompt: PromptReference     => prompt.asJson
      case resource: ResourceReference => resource.asJson
    }

  final case class PromptReference(
    `type`: String,
    uri:    String
  ) extends PromptOrResourceReference
  object PromptReference:
    given Decoder[PromptReference] = Decoder.derived[PromptReference]
    given Encoder[PromptReference] = Encoder.derived[PromptReference]

  final case class ResourceReference(
    `type`: String,
    uri:    String
  ) extends PromptOrResourceReference
  object ResourceReference:
    given Decoder[ResourceReference] = Decoder.derived[ResourceReference]
    given Encoder[ResourceReference] = Encoder.derived[ResourceReference]

  final case class CompleteArgument(
    name:  String,
    value: String
  )
  object CompleteArgument:
    given Decoder[CompleteArgument] = Decoder.derived[CompleteArgument]
    given Encoder[CompleteArgument] = Encoder.derived[CompleteArgument]

  // ---------------------------
  // Autocomplete
  // ---------------------------
  final case class CompleteRequest(
    ref:      PromptOrResourceReference,
    argument: CompleteArgument
  ) extends Request
  object CompleteRequest:
    given Decoder[CompleteRequest] = Decoder.derived[CompleteRequest]
    given Encoder[CompleteRequest] = Encoder.derived[CompleteRequest]

  final case class CompleteCompletion(
    values:  List[String],
    total:   Int,
    hasMore: Boolean
  )
  object CompleteCompletion:
    given Decoder[CompleteCompletion] = Decoder.derived[CompleteCompletion]
    given Encoder[CompleteCompletion] = Encoder.derived[CompleteCompletion]

  final case class CompleteResult(
    completion: CompleteCompletion
  )
  object CompleteResult:
    given Decoder[CompleteResult] = Decoder.derived[CompleteResult]
    given Encoder[CompleteResult] = Encoder.derived[CompleteResult]

  // ---------------------------
  // Roots
  // ---------------------------
  /**
   * Represents a root directory or file that the server can operate on.
   *
   * @param uri  The URI identifying the root. This *must* start with file:// for now.
   *             This restriction may be relaxed in future versions of the protocol to allow other
   *             URI schemes.
   * @param name An optional name for the root. This can be used to provide a
   *             human-readable identifier for the root, which may be useful for display purposes or
   *             for referencing the root in other parts of the application.
   */
  final case class Root(
    uri:  String,
    name: Option[String]
  )
  object Root:
    given Decoder[Root] = Decoder.derived[Root]
    given Encoder[Root] = Encoder.derived[Root]

  /**
   * The client's response to a roots/list request from the server. This result contains
   * an array of Root objects, each representing a root directory or file that the
   * server can operate on.
   *
   * @param roots An array of Root objects, each representing a root directory or file
   *              that the server can operate on.
   */
  final case class ListRootsResult(
    roots: List[Root]
  )
  object ListRootsResult:
    given Decoder[ListRootsResult] = Decoder.derived[ListRootsResult]
    given Encoder[ListRootsResult] = Encoder.derived[ListRootsResult]
