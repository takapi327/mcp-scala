/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

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
      case "text" => c.as[Text]
      case "blob" => c.as[Blob]
      case _ => Left(DecodingFailure("Unknown resource type", c.history))
    }
  }

  given Encoder[ResourceContents] = Encoder.instance {
    case text: Text => text.asJson
    case blob: Blob => blob.asJson
  }

  /**
   * Text contents of a resource.
   *
   * @param uri      the URI of this resource.
   * @param mimeType the MIME type of this resource.
   * @param text     the text of the resource. This must only be set if the resource can
   *                 actually be represented as text (not binary data).
   */
  final case class Text(
                         uri:      String,
                         mimeType: String,
                         text:     String
                       ) extends ResourceContents

  object Text:
    given Decoder[Text] = Decoder.derived[Text]
    given Encoder[Text] = Encoder.derived[Text]

  /**
   * Binary contents of a resource.
   *
   * @param uri      the URI of this resource.
   * @param mimeType the MIME type of this resource.
   * @param blob     a base64-encoded string representing the binary data of the resource.
   *                 This must only be set if the resource can actually be represented as binary data
   *                 (not text).
   */
  final case class Blob(
                                         uri:      String,
                                         mimeType: String,
                                         blob:     String
                                       ) extends ResourceContents
  object Blob:
    given Decoder[Blob] = Decoder.derived[Blob]
    given Encoder[Blob] = Encoder.derived[Blob]
