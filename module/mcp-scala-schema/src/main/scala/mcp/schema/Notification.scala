/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

import mcp.schema.notification.Notification

object Notification:

  /**
   * An optional notification from the server to the client, informing it that the list of prompts it offers has changed. This may be issued by servers without any previous subscription from the client.
   */
  final case class PromptListChangedNotification() extends Notification:
    override def method: Method = Method.METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED
  object PromptListChangedNotification:
    given Decoder[PromptListChangedNotification] = Decoder.instance { cursor =>
      for {
        method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED)
      } yield
        if method then PromptListChangedNotification()
        else throw new Exception("Invalid method for PromptListChangedNotification")
    }

    given Encoder[PromptListChangedNotification] = Encoder.instance { list =>
      Json.obj(
        "method" -> list.method.asJson
      )
    }

  final case class ToolListChangedNotification() extends Notification:
    override def method: Method = Method.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED
  object ToolListChangedNotification:
    given Decoder[ToolListChangedNotification] = Decoder.instance { cursor =>
      for {
        method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED)
      } yield
        if method then ToolListChangedNotification()
        else throw new Exception("Invalid method for ToolListChangedNotification")
    }

    given Encoder[ToolListChangedNotification] = Encoder.instance { list =>
      Json.obj(
        "method" -> list.method.asJson
      )
    }

  /**
   * Notification of a log message passed from server to client. If no logging/setLevel request has been sent from the client, the server MAY decide which messages to send automatically.
   */
  final case class LoggingMessageNotification(level: LoggingLevel, logger: Option[String], data: Json)
    extends Notification:
    override def method: Method = Method.METHOD_NOTIFICATION_MESSAGE
  object LoggingMessageNotification:
    given Decoder[LoggingMessageNotification] = Decoder.instance { cursor =>
      for {
        method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_MESSAGE)
        level  <- cursor.get[LoggingLevel]("level")
        logger <- cursor.get[Option[String]]("logger")
        data   <- cursor.get[Json]("data")
      } yield
        if method then LoggingMessageNotification(level, logger, data)
        else throw new Exception("Invalid method for LoggingMessageNotification")
    }

    given Encoder[LoggingMessageNotification] = Encoder.instance { message =>
      Json.obj(
        "method" -> message.method.asJson,
        "params" -> Json.obj(
          "level"  -> message.level.asJson,
          "logger" -> message.logger.asJson,
          "data"   -> message.data.asJson
        )
      )
    }

  final case class RootsListChangedNotification() extends Notification:
    override def method: Method = Method.METHOD_NOTIFICATION_ROOTS_LIST_CHANGED
  object RootsListChangedNotification:
    given Decoder[RootsListChangedNotification] = Decoder.instance { cursor =>
      for {
        method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_ROOTS_LIST_CHANGED)
      } yield
        if method then RootsListChangedNotification()
        else throw new Exception("Invalid method for RootsListChangedNotification")
    }

    given Encoder[RootsListChangedNotification] = Encoder.instance { list =>
      Json.obj(
        "method" -> list.method.asJson
      )
    }

  /**
   * This notification can be sent by either side to indicate that it is cancelling a previously-issued request.
   *
   * The request SHOULD still be in-flight, but due to communication latency, it is always possible that this notification MAY arrive after the request has already finished.
   *
   * This notification indicates that the result will be unused, so any associated processing SHOULD cease.
   *
   * A client MUST NOT attempt to cancel its `initialize` request.
   */
  final case class CancelledNotification(params: Option[Json]) extends Notification:
    override def method: Method = Method.METHOD_CANCEL_NOTIFICATIONS
  object CancelledNotification:
    given Decoder[CancelledNotification] = Decoder.instance { cursor =>
      for {
        method <- cursor.get[Method]("method").map(_ == Method.METHOD_CANCEL_NOTIFICATIONS)
        params <- cursor.get[Option[Json]]("params")
      } yield
        if method then CancelledNotification(params)
        else throw new Exception("Invalid method for CancelledNotification")
    }
    given Encoder[CancelledNotification] = Encoder.instance { cancel =>
      Json
        .obj(
          "method" -> cancel.method.asJson,
          "params" -> cancel.params.asJson
        )
        .dropNullValues
    }
