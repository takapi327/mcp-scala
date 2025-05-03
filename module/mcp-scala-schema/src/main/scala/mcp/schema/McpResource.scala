/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import cats.syntax.all.*

import io.circe.*
import io.circe.syntax.*

trait McpResource:
  def name: String

  def description: Option[String]

  def mimeType: Option[String]

  private[mcp] def isStatic: Boolean

object McpResource:

  def static(
              uri: String,
              name: String,
              description: Option[String],
              mimeType: Option[String],
              annotations: Annotations
            ): Static = Static(uri, name, description, mimeType, annotations)
  
  def template(
               uriTemplate: String,
               name:        String,
               description: Option[String],
               mimeType:    Option[String],
               annotations: Annotations
             ): Template = Template(uriTemplate, name, description, mimeType, annotations)

  given Decoder[McpResource] = List[Decoder[McpResource]](
    Decoder[Static].widen,
    Decoder[Template].widen
  ).reduceLeft(_ or _)

  given Encoder[McpResource] = Encoder.instance {
    case static: Static => static.asJson
    case template: Template => template.asJson
  }

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
  final case class Static(
                           uri: String,
                           name: String,
                           description: Option[String],
                           mimeType: Option[String],
                           annotations: Annotations
                         ) extends McpResource:
    override private[mcp] def isStatic: Boolean = true

  object Static:
    given Decoder[Static] = Decoder.derived[Static]
    given Encoder[Static] = Encoder.derived[Static].mapJson(_.dropNullValues)

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
  final case class Template(
                                     uriTemplate: String,
                                     name:        String,
                                     description: Option[String],
                                     mimeType:    Option[String],
                                     annotations: Annotations
                                   ) extends McpResource:

    override private[mcp] def isStatic: Boolean = false

  object Template:
    given Decoder[Template] = Decoder.derived[Template]
    given Encoder[Template] = Encoder.derived[Template].mapJson(_.dropNullValues)
