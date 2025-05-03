/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

sealed trait Content:

  def `type`: String

object Content:
  given Decoder[Content] = Decoder.instance { c =>
    c.get[String]("type").flatMap {
      case "text"     => c.as[Text]
      case "image"    => c.as[Image]
      case "resource" => c.as[Embedded]
      case _          => Left(DecodingFailure("Unknown content type", c.history))
    }
  }

  given Encoder[Content] = Encoder.instance {
    case text: Text         => text.asJson
    case image: Image       => image.asJson
    case resource: Embedded => resource.asJson
  }

  def text(text: String): Text = Text(None, None, text)
  def image(audience: List[Role], priority: Double, data: String, mimeType: String): Image =
    Image(audience, priority, data, mimeType)
  def embedded(audience: List[Role], priority: Double, resource: ResourceContents): Embedded =
    Embedded(audience, priority, resource)

  final case class Text(
    audience: Option[List[Role]],
    priority: Option[Double],
    text:     String
  ) extends Content:
    override def `type`: String = "text"

  object Text:
    given Decoder[Text] = Decoder.derived[Text]
    given Encoder[Text] = Encoder.instance { txt =>
      Json
        .obj(
          "type"     -> Json.fromString(txt.`type`),
          "audience" -> txt.audience.asJson,
          "priority" -> txt.priority.asJson,
          "text"     -> Json.fromString(txt.text)
        )
        .dropNullValues
    }

  final case class Image(
    audience: List[Role],
    priority: Double,
    data:     String,
    mimeType: String
  ) extends Content:
    override def `type`: String = "image"

  object Image:
    given Decoder[Image] = Decoder.derived[Image]
    given Encoder[Image] = Encoder.derived[Image]

  final case class Embedded(
    audience: List[Role],
    priority: Double,
    resource: ResourceContents
  ) extends Content:
    override def `type`: String = "resource"

  object Embedded:
    given Decoder[Embedded] = Decoder.derived[Embedded]
    given Encoder[Embedded] = Encoder.derived[Embedded]
