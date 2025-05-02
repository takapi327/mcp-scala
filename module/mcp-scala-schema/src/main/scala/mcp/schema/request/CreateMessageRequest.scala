/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema
package request

import io.circe.*
import io.circe.syntax.*

import mcp.schema.McpSchema.{ ContextInclusionStrategy, ModelPreferences, SamplingMessage }

/**
 * A request from the server to sample an LLM via the client. The client has full discretion over which model to select. The client should also inform the user before beginning sampling, to allow them to inspect the request (human in the loop) and decide whether to approve it.
 */
final case class CreateMessageRequest(
  messages:         List[SamplingMessage],
  modelPreferences: Option[ModelPreferences],
  systemPrompt:     Option[String],
  includeContext:   Option[ContextInclusionStrategy],
  temperature:      Option[String],
  maxTokens:        Int,
  stopSequences:    Option[Int],
  metaData:         Option[Json]
) extends Request:
  override def method: Method = Method.METHOD_NOTIFICATION_MESSAGE

object CreateMessageRequest:
  given Decoder[CreateMessageRequest] = Decoder.instance { cursor =>
    for {
      messages         <- cursor.get[List[SamplingMessage]]("messages")
      modelPreferences <- cursor.get[Option[ModelPreferences]]("modelPreferences")
      systemPrompt     <- cursor.get[Option[String]]("systemPrompt")
      includeContext   <- cursor.get[Option[ContextInclusionStrategy]]("includeContext")
      temperature      <- cursor.get[Option[String]]("temperature")
      maxTokens        <- cursor.get[Int]("maxTokens")
      stopSequences    <- cursor.get[Option[Int]]("stopSequences")
      metaData         <- cursor.get[Option[Json]]("metaData")
    } yield CreateMessageRequest(
      messages,
      modelPreferences,
      systemPrompt,
      includeContext,
      temperature,
      maxTokens,
      stopSequences,
      metaData
    )
  }

  given Encoder[CreateMessageRequest] = Encoder.instance { create =>
    Json
      .obj(
        "method" -> create.method.asJson,
        "params" -> Json.obj(
          "messages"         -> create.messages.asJson,
          "modelPreferences" -> create.modelPreferences.asJson,
          "systemPrompt"     -> create.systemPrompt.asJson,
          "includeContext"   -> create.includeContext.asJson,
          "temperature"      -> create.temperature.asJson,
          "maxTokens"        -> create.maxTokens.asJson,
          "stopSequences"    -> create.stopSequences.asJson,
          "metaData"         -> create.metaData.asJson
        )
      )
      .dropNullValues
  }
