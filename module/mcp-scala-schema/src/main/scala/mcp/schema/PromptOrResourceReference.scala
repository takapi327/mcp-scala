/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

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
