package mcp.spec

import io.circe.*
import io.circe.syntax.*

/**
 * Based on the <a href="http://www.jsonrpc.org/specification">JSON-RPC 2.0
 * specification</a> and the <a href=
 * "https://github.com/modelcontextprotocol/specification/blob/main/schema/schema.ts">Model
 * Context Protocol Schema</a>.
 *
 */
class McpSchema

object McpSchema:

  val LATEST_PROTOCOL_VERSION: String = "2024-11-05"

  val JSONRPC_VERSION: String = "2.0"

  // ---------------------------
  // Method Names
  // ---------------------------

  // Lifecycle Methods
  val METHOD_INITIALIZE: String = "initialize"
  val METHOD_NOTIFICATION_INITIALIZED: String = "notifications/initialized"
  val METHOD_PING: String = "ping"

  // Tool Methods
  val METHOD_TOOLS_LIST: String = "tools/list"
  val METHOD_TOOLS_CALL: String = "tools/call"
  val METHOD_NOTIFICATION_TOOLS_LIST_CHANGED: String = "notifications/tools/list_changed"

  // Resources Methods
  val METHOD_RESOURCES_LIST: String = "resources/list"
  val METHOD_RESOURCES_READ: String = "resources/read"
  val METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED: String = "notifications/resources/list_changed"
  val METHOD_RESOURCES_TEMPLATES_LIST: String = "resources/templates/list"
  val METHOD_RESOURCES_SUBSCRIBE: String = "resources/subscribe"
  val METHOD_RESOURCES_UNSUBSCRIBE: String = "resources/unsubscribe"

  // Prompt Methods
  val METHOD_PROMPT_LIST: String = "prompts/list"
  val METHOD_PROMPT_GET: String = "prompts/get"
  val METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED: String = "notifications/prompts/list_changed"

  // Logging Methods
  val METHOD_LOGGING_SET_LEVEL: String = "logging/setLevel"
  val METHOD_NOTIFICATION_MESSAGE: String = "notifications/message"

  // Roots Methods
  val METHOD_ROOTS_LIST: String = "roots/list"
  val METHOD_NOTIFICATION_ROOTS_LIST_CHANGED: String = "notifications/roots/list_changed"

  // Sampling Methods
  val METHOD_SAMPLING_CREATE_MESSAGE: String = "sampling/createMessage"

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

  final case class JSONRPCRequest(
    jsonrpc: String,
    method: String,
    id: String,
    params: Json
  ) extends JSONRPCMessage

  object JSONRPCRequest:
    given Decoder[JSONRPCRequest] = Decoder.derived[JSONRPCRequest]
    given Encoder[JSONRPCRequest] = Encoder.derived[JSONRPCRequest]

  final case class JSONRPCNotification(
                                        jsonrpc: String,
                                        method: String,
                                        params: Json
                                      ) extends JSONRPCMessage

  object JSONRPCNotification:
    given Decoder[JSONRPCNotification] = Decoder.derived[JSONRPCNotification]
    given Encoder[JSONRPCNotification] = Encoder.derived[JSONRPCNotification]

  final case class JSONRPCError(
    code: Int,
    message: String,
    data: Option[Json]
  )

  object JSONRPCError:
    given Decoder[JSONRPCError] = Decoder.derived[JSONRPCError]
    given Encoder[JSONRPCError] = Encoder.derived[JSONRPCError]

  final case class JSONRPCResponse(
    jsonrpc: String,
    id: String,
    result: Option[Json],
    error: Option[JSONRPCError]
  ) extends JSONRPCMessage

  object JSONRPCResponse:
    given Decoder[JSONRPCResponse] = Decoder.derived[JSONRPCResponse]
    given Encoder[JSONRPCResponse] = Encoder.derived[JSONRPCResponse]

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
                                       experimental: Map[String, Json],
                                        roots: Option[RootCapabilities],
                                       sampling: Option[Sampling]
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
                                       subscribe: Boolean,
                                       listChanged: Boolean,
                                       )
  object ResourceCapabilities:
    given Decoder[ResourceCapabilities] = Decoder.derived[ResourceCapabilities]
    given Encoder[ResourceCapabilities] = Encoder.derived[ResourceCapabilities]

  final case class ToolCapabilities(listChanged: Boolean)
  object ToolCapabilities:
    given Decoder[ToolCapabilities] = Decoder.derived[ToolCapabilities]
    given Encoder[ToolCapabilities] = Encoder.derived[ToolCapabilities]

  final case class ServerCapabilities(
                                        experimental: Map[String, Json],
                                        logging: Option[LoggingCapabilities],
                                        prompt: Option[PromptCapabilities],
                                        resources: Option[ResourceCapabilities],
                                        tools: Option[ToolCapabilities],
                                     )
  object ServerCapabilities:
    given Decoder[ServerCapabilities] = Decoder.derived[ServerCapabilities]
    given Encoder[ServerCapabilities] = Encoder.derived[ServerCapabilities]

  final case class Implementation(
    name: String,
    version: String,
                                 )
  object Implementation:
    given Decoder[Implementation] = Decoder.derived[Implementation]
    given Encoder[Implementation] = Encoder.derived[Implementation]

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

  // ---------------------------
  // Initialization
  // ---------------------------
  final case class InitializeRequest(
                                      protocolVersion: String,
                                      capabilities: ClientCapabilities,
                                      clientInfo: Implementation,
                                    ) extends Request
  object InitializeRequest:
    given Decoder[InitializeRequest] = Decoder.derived[InitializeRequest]
    given Encoder[InitializeRequest] = Encoder.derived[InitializeRequest]

  final case class InitializeResult(
                                   protocolVersion: String,
                                    capabilities: ServerCapabilities,
                                   serverInfo: Implementation,
                                   instructions: String
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
                              priority: Double
                              )
  object Annotations:
    given Decoder[Annotations] = Decoder.derived[Annotations]
    given Encoder[Annotations] = Encoder.derived[Annotations]

  // ---------------------------
  // Resource Interfaces
  // ---------------------------
  /**
   * Base for objects that include optional annotations for the client. The client can
   * use annotations to inform how objects are used or displayed
   */
  sealed trait Annotated:
    def annotations: Annotations

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
  final case class Resource(
                           uri: String,
                           name: String,
                           description: String,
                           mimeType: String,
                            annotations: Annotations,
                           ) extends Annotated

  object Resource:
    given Decoder[Resource] = Decoder.derived[Resource]
    given Encoder[Resource] = Encoder.derived[Resource]

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
                                    name: String,
                                    description: String,
                                    mimeType: String,
                                    annotations: Annotations,
                                   ) extends Annotated

  object ResourceTemplate:
    given Decoder[ResourceTemplate] = Decoder.derived[ResourceTemplate]
    given Encoder[ResourceTemplate] = Encoder.derived[ResourceTemplate]

  final case class ListResourcesResult(
    resources: List[Resource],
    nextCursor: String
                                      )
  object ListResourcesResult:
    given Decoder[ListResourcesResult] = Decoder.derived[ListResourcesResult]
    given Encoder[ListResourcesResult] = Encoder.derived[ListResourcesResult]

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
        case _ => Left(DecodingFailure("Unknown resource type", c.history))
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
    uri: String,
    mimeType: String,
    text: String,
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
    uri: String,
    mimeType: String,
    blob: String,
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
    name: String,
    description: String,
    required: Boolean,
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
    name: String,
    description: String,
    arguments: List[PromptArgument],
  )
  object Prompt:
    given Decoder[Prompt] = Decoder.derived[Prompt]
    given Encoder[Prompt] = Encoder.derived[Prompt]

  // ---------------------------
  // Content Types
  // ---------------------------
  sealed trait Content:

    def `type`: String
    
  object Content:
    given Decoder[Content] = Decoder.instance { c =>
      c.get[String]("type").flatMap {
        case "text" => c.as[TextContent]
        case "image" => c.as[ImageContent]
        case "resource" => c.as[EmbeddedResource]
        case _ => Left(DecodingFailure("Unknown content type", c.history))
      }
    }

    given Encoder[Content] = Encoder.instance {
      case text: TextContent => text.asJson
      case image: ImageContent => image.asJson
      case resource: EmbeddedResource => resource.asJson
    }

  final case class TextContent(
                              audience: List[Role],
                              priority: Double,
    text: String,
  ) extends Content:
    override def `type`: String = "text"

  object TextContent:
    given Decoder[TextContent] = Decoder.derived[TextContent]
    given Encoder[TextContent] = Encoder.derived[TextContent]

  final case class ImageContent(
                              audience: List[Role],
                              priority: Double,
                              data: String,
                              mimeType: String,
                               ) extends Content:
    override def `type`: String = "image"

  object ImageContent:
    given Decoder[ImageContent] = Decoder.derived[ImageContent]
    given Encoder[ImageContent] = Encoder.derived[ImageContent]

  final case class EmbeddedResource(
                                   audience: List[Role],
                                    priority: Double,
                                   resource: ResourceContents,
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
    role: Role,
    content: Content,
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
    prompts: List[Prompt],
    nextCursor: String
                                    )
  object ListPromptsResult:
    given Decoder[ListPromptsResult] = Decoder.derived[ListPromptsResult]
    given Encoder[ListPromptsResult] = Encoder.derived[ListPromptsResult]

  /**
   * The server's response to a prompts/get request from the client.
   *
   * @param description An optional description for the prompt.
   * @param messages    A list of messages to display as part of the prompt.
   */
  final case class GetPromptRequest(
                                   description: String,
                                   messages: List[PromptMessage],
                                   )
  object GetPromptRequest:
    given Decoder[GetPromptRequest] = Decoder.derived[GetPromptRequest]
    given Encoder[GetPromptRequest] = Encoder.derived[GetPromptRequest]

  final case class JsonSchema(
                             `type`: String,
                              properties: Map[String, Json],
                              required: List[String],
                              additionalProperties: Boolean,
                             )
  object JsonSchema:
    given Decoder[JsonSchema] = Decoder.derived[JsonSchema]
    given Encoder[JsonSchema] = Encoder.derived[JsonSchema]

  /**
   * Represents a tool that the server provides. Tools enable servers to expose
   * executable functionality to the system. Through these tools, you can interact with
   * external systems, perform computations, and take actions in the real world.
   *
   * @param name        A unique identifier for the tool. This name is used when calling the
   *                    tool.
   * @param description A human-readable description of what the tool does. This can be
   *                    used by clients to improve the LLM's understanding of available tools.
   * @param inputSchema A JSON Schema object that describes the expected structure of
   *                    the arguments when calling this tool. This allows clients to validate tool
   *                    arguments before sending them to the server.
   */
  final case class Tool(
                       name: String,
                        description: String,
                       inputSchema: JsonSchema
                       )
  object Tool:
    given Decoder[Tool] = Decoder.derived[Tool]
    given Encoder[Tool] = Encoder.derived[Tool]

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
    tools: List[Tool],
    nextCursor: String
                                  )
  object ListToolsResult:
    given Decoder[ListToolsResult] = Decoder.derived[ListToolsResult]
    given Encoder[ListToolsResult] = Encoder.derived[ListToolsResult]

  /**
   * Used by the client to call a tool provided by the server.
   *
   * @param name      The name of the tool to call. This must match a tool name from
   *                  tools/list.
   * @param arguments Arguments to pass to the tool. These must conform to the tool's
   *                  input schema.
   */
  final case class CallToolRequest(
    name: String,
    arguments: Map[String, Json],
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
                                 isError: Boolean,
                                 )
  object CallToolResult:
    given Decoder[CallToolResult] = Decoder.derived[CallToolResult]
    given Encoder[CallToolResult] = Encoder.derived[CallToolResult]

  final case class ModelHint(name: String)
  object ModelHint:
    given Decoder[ModelHint] = Decoder.derived[ModelHint]
    given Encoder[ModelHint] = Encoder.derived[ModelHint]

  // ---------------------------
  // Sampling Interfaces
  // ---------------------------
  final case class ModelPreferences(
                                   hints: List[ModelHint],
                                   costPriority: Double,
                                   speedPriority: Double,
                                   intelligencePriority: Double,
                                   )
  object ModelPreferences:
    given Decoder[ModelPreferences] = Decoder.derived[ModelPreferences]
    given Encoder[ModelPreferences] = Encoder.derived[ModelPreferences]

  final case class SamplingMessage(
    role: Role,
    content: Content,
                                  )
  object SamplingMessage:
    given Decoder[SamplingMessage] = Decoder.derived[SamplingMessage]
    given Encoder[SamplingMessage] = Encoder.derived[SamplingMessage]

  enum ContextInclusionStrategy:
    case NONE, THIS_SERVER, ALL_SERVERS
  object ContextInclusionStrategy:
    given Decoder[ContextInclusionStrategy] = Decoder[String].map {
      case "none" => ContextInclusionStrategy.NONE
      case "thisServer" => ContextInclusionStrategy.THIS_SERVER
      case "allServers" => ContextInclusionStrategy.ALL_SERVERS
    }
    given Encoder[ContextInclusionStrategy] = Encoder[String].contramap {
      case ContextInclusionStrategy.NONE => "none"
      case ContextInclusionStrategy.THIS_SERVER => "thisServer"
      case ContextInclusionStrategy.ALL_SERVERS => "allServers"
    }


  final case class CreateMessageRequest(
                                       messages: List[SamplingMessage],
                                        modelPreferences: ModelPreferences,
                                       systemPrompt: Option[String],
                                       includeContext: ContextInclusionStrategy,
                                       temperature: Double,
                                       maxTokens: Int,
                                       stopSequences: List[String],
                                       metadata: Map[String, Json],
                                       ) extends Request
  object CreateMessageRequest:
    given Decoder[CreateMessageRequest] = Decoder.derived[CreateMessageRequest]
    given Encoder[CreateMessageRequest] = Encoder.derived[CreateMessageRequest]

  enum StopReason:
    case END_TURN, STOP_SEQUENCE, MAX_TOKENS
  object StopReason:
    given Decoder[StopReason] = Decoder[String].map {
      case "endTurn" => StopReason.END_TURN
      case "stopSequence" => StopReason.STOP_SEQUENCE
      case "maxTokens" => StopReason.MAX_TOKENS
    }
    given Encoder[StopReason] = Encoder[String].contramap {
      case StopReason.END_TURN => "endTurn"
      case StopReason.STOP_SEQUENCE => "stopSequence"
      case StopReason.MAX_TOKENS => "maxTokens"
    }

  final case class CreateMessageResult(
                                      role: Role,
                                      content: Content,
                                      model: String,
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
    progress: Double,
    total: Double,
                                       )
  object ProgressNotification:
    given Decoder[ProgressNotification] = Decoder.derived[ProgressNotification]
    given Encoder[ProgressNotification] = Encoder.derived[ProgressNotification]

  enum LoggingLevel(val code: Int):
    case DEBUG extends LoggingLevel(0)
    case INFO extends LoggingLevel(1)
    case NOTICE extends LoggingLevel(2)
    case WARNING extends LoggingLevel(3)
    case ERROR extends LoggingLevel(4)
    case CRITICAL extends LoggingLevel(5)
    case ALERT extends LoggingLevel(6)
    case EMERGENCY extends LoggingLevel(7)
  object LoggingLevel:
    given Decoder[LoggingLevel] = Decoder[Int].map {
      case 0 => LoggingLevel.DEBUG
      case 1 => LoggingLevel.INFO
      case 2 => LoggingLevel.NOTICE
      case 3 => LoggingLevel.WARNING
      case 4 => LoggingLevel.ERROR
      case 5 => LoggingLevel.CRITICAL
      case 6 => LoggingLevel.ALERT
      case 7 => LoggingLevel.EMERGENCY
    }
    given Encoder[LoggingLevel] = Encoder[Int].contramap(_.code)

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
                                             level: LoggingLevel,
                                             logger: String,
                                              data: String,
                                             )
  object LoggingMessageNotification:
    given Decoder[LoggingMessageNotification] = Decoder.derived[LoggingMessageNotification]
    given Encoder[LoggingMessageNotification] = Encoder.derived[LoggingMessageNotification]

  sealed trait PromptOrResourceReference:

    def `type`: String
    
  object PromptOrResourceReference:
    given Decoder[PromptOrResourceReference] = Decoder.instance { c =>
      c.get[String]("type").flatMap {
        case "prompt" => c.as[PromptReference]
        case "resource" => c.as[ResourceReference]
        case _ => Left(DecodingFailure("Unknown reference type", c.history))
      }
    }

    given Encoder[PromptOrResourceReference] = Encoder.instance {
      case prompt: PromptReference => prompt.asJson
      case resource: ResourceReference => resource.asJson
    }

  final case class PromptReference(
                                  `type`: String,
                                  uri: String,
                                  ) extends PromptOrResourceReference
  object PromptReference:
    given Decoder[PromptReference] = Decoder.derived[PromptReference]
    given Encoder[PromptReference] = Encoder.derived[PromptReference]

  final case class ResourceReference(
                                    `type`: String,
                                    uri: String,
                                    ) extends PromptOrResourceReference
  object ResourceReference:
    given Decoder[ResourceReference] = Decoder.derived[ResourceReference]
    given Encoder[ResourceReference] = Encoder.derived[ResourceReference]

  final case class CompleteArgument(
                                    name: String,
                                    value: String,
                                   )
  object CompleteArgument:
    given Decoder[CompleteArgument] = Decoder.derived[CompleteArgument]
    given Encoder[CompleteArgument] = Encoder.derived[CompleteArgument]

  // ---------------------------
  // Autocomplete
  // ---------------------------
  final case class CompleteRequest(
                                    ref: PromptOrResourceReference,
                                    argument: CompleteArgument
                                  ) extends Request
  object CompleteRequest:
    given Decoder[CompleteRequest] = Decoder.derived[CompleteRequest]
    given Encoder[CompleteRequest] = Encoder.derived[CompleteRequest]

  final case class CompleteCompletion(
                                     values: List[String],
                                     total: Int,
                                     hasMore: Boolean,
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
    uri: String,
    name: Option[String],
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