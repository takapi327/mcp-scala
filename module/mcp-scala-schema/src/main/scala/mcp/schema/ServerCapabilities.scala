/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

final case class ServerCapabilities(
  experimental:         Option[Map[String, Json]],
  logging:              Option[Json],
  completions:          Option[Json],
  promptsListChanged:   Option[Boolean],
  resourcesSubscribe:   Option[Boolean],
  resourcesListChanged: Option[Boolean],
  toolsListChanged:     Option[Boolean]
):

  def withExperimental(value: Map[String, Json]): ServerCapabilities =
    this.copy(experimental = Some(value))

  def withLogging(value: Json): ServerCapabilities =
    this.copy(logging = Some(value))

  def withCompletions(value: Json): ServerCapabilities =
    this.copy(completions = Some(value))

  def withPromptsListChanged(value: Boolean): ServerCapabilities =
    this.copy(promptsListChanged = Some(value))

  def withResourcesSubscribe(value: Boolean): ServerCapabilities =
    this.copy(resourcesSubscribe = Some(value))

  def withResourcesListChanged(value: Boolean): ServerCapabilities =
    this.copy(resourcesListChanged = Some(value))

  def withToolsListChanged(value: Boolean): ServerCapabilities =
    this.copy(toolsListChanged = Some(value))

object ServerCapabilities:

  def build(): ServerCapabilities = ServerCapabilities(
    experimental         = None,
    logging              = None,
    completions          = None,
    promptsListChanged   = None,
    resourcesSubscribe   = None,
    resourcesListChanged = None,
    toolsListChanged     = None
  )

  given Decoder[ServerCapabilities] = Decoder.instance { cursor =>
    for {
      experimental         <- cursor.get[Option[Map[String, Json]]]("experimental")
      logging              <- cursor.get[Option[Json]]("logging")
      completions          <- cursor.get[Option[Json]]("completions")
      promptsListChanged   <- cursor.get[Option[Boolean]]("prompts.listChanged")
      resourcesSubscribe   <- cursor.get[Option[Boolean]]("resources.subscribe")
      resourcesListChanged <- cursor.get[Option[Boolean]]("resources.listChanged")
      toolsListChanged     <- cursor.get[Option[Boolean]]("tools.listChanged")
    } yield ServerCapabilities(
      experimental,
      logging,
      completions,
      promptsListChanged,
      resourcesSubscribe,
      resourcesListChanged,
      toolsListChanged
    )
  }

  given Encoder[ServerCapabilities] = Encoder.instance { capabilities =>
    Json
      .obj(
        "experimental" -> capabilities.experimental.asJson,
        "logging"      -> capabilities.logging.asJson,
        "completions"  -> capabilities.completions.asJson,
        "prompts" -> Json.obj(
          "listChanged" -> capabilities.promptsListChanged.asJson
        ).dropNullValues,
        "resources" -> Json.obj(
          "subscribe"   -> capabilities.resourcesSubscribe.asJson,
          "listChanged" -> capabilities.resourcesListChanged.asJson
        ).dropNullValues,
        "tools" -> Json.obj(
          "listChanged" -> capabilities.toolsListChanged.asJson
        ).dropNullValues
      )
      .dropNullValues
  }
