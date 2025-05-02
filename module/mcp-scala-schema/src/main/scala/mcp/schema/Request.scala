/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

import mcp.schema.McpSchema.{
  CompleteArgument,
  ContextInclusionStrategy,
  ModelPreferences,
  PromptOrResourceReference,
  SamplingMessage
}

import mcp.schema.request.*

object Request:

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

  final case class CompleteRequest(ref: PromptOrResourceReference, argument: CompleteArgument) extends Request:
    override def method: Method = Method.METHOD_COMPLETION_COMPLETE
  object CompleteRequest:
    given Decoder[CompleteRequest] = Decoder.instance { cursor =>
      for {
        ref      <- cursor.get[PromptOrResourceReference]("ref")
        argument <- cursor.get[CompleteArgument]("argument")
      } yield CompleteRequest(ref, argument)
    }

    given Encoder[CompleteRequest] = Encoder.instance { complete =>
      Json
        .obj(
          "method" -> complete.method.asJson,
          "params" -> Json.obj(
            "ref"      -> complete.ref.asJson,
            "argument" -> complete.argument.asJson
          )
        )
        .dropNullValues
    }

  /**
   * Sent from the server to request a list of root URIs from the client. Roots allow
   * servers to ask for specific directories or files to operate on. A common example
   * for roots is providing a set of repositories or directories a server should operate
   * on.
   *
   * This request is typically used when the server needs to understand the file system
   * structure or access specific locations that the client has permission to read from.
   */
  final case class ListRootsRequest() extends Request:
    override def method: Method = Method.METHOD_ROOTS_LIST
  object ListRootsRequest:
    given Decoder[ListRootsRequest] = Decoder.derived[ListRootsRequest]

    given Encoder[ListRootsRequest] = Encoder.instance { list =>
      Json.obj(
        "method" -> list.method.asJson
      )
    }
