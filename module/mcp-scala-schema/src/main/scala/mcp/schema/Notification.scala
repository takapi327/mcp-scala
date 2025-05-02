/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

trait Notification:

  def method: Method

object Notification:

  /**
   * This notification is sent from the client to the server after initialization has finished.
   */
  final case class InitializedNotification() extends Notification:
    override def method: Method = Method.METHOD_NOTIFICATION_INITIALIZED
  object InitializedNotification:
    given Decoder[InitializedNotification] = Decoder.instance { cursor =>
      for {
        method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_INITIALIZED)
      } yield
        if method then InitializedNotification()
        else throw new Exception("Invalid method for InitializedNotification")
    }

    given Encoder[InitializedNotification] = Encoder.instance { init =>
      Json.obj(
        "method" -> init.method.asJson,
      )
    }

  /**
   * An out-of-band notification used to inform the receiver of a progress update for a long-running request.
   */
  final case class ProgressNotification(
                                         progressToken: ProgressToken,
                                         progress: Int,
                                         total: Option[Int],
                                         message: Option[String],
                                       ) extends Notification:
    override def method: Method = Method.METHOD_NOTIFICATION_PROGRESS
  object ProgressNotification:
    given Decoder[ProgressNotification] = Decoder.instance { cursor =>
      for {
        method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_PROGRESS)
        progressToken <- cursor.get[ProgressToken]("progressToken")
        progress <- cursor.get[Int]("progress")
        total <- cursor.get[Option[Int]]("total")
        message <- cursor.get[Option[String]]("message")
      } yield
        if method then ProgressNotification(progressToken, progress, total, message)
        else throw new Exception("Invalid method for ProgressNotification")
    }
    given Encoder[ProgressNotification] = Encoder.instance { progress =>
      Json.obj(
        "method" -> progress.method.asJson,
        "params" -> Json.obj(
          "progressToken" -> progress.progressToken.asJson,
          "progress" -> progress.progress.asJson,
          "total" -> progress.total.asJson,
          "message" -> progress.message.asJson,
        )
      )
    }

  /**
   * An optional notification from the server to the client, informing it that the list of resources it can read from has changed. This may be issued by servers without any previous subscription from the client.
   */
  final case class ResourceListChangedNotification() extends Notification:
    override def method: Method = Method.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED
  object ResourceListChangedNotification:
    given Decoder[ResourceListChangedNotification] = Decoder.instance { cursor =>
      for {
        method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED)
      } yield
        if method then ResourceListChangedNotification()
        else throw new Exception("Invalid method for ResourceListChangedNotification")
    }

    given Encoder[ResourceListChangedNotification] = Encoder.instance { list =>
      Json.obj(
        "method" -> list.method.asJson,
      )
    }

  /**
   * A notification from the server to the client, informing it that a resource has changed and may need to be read again. This should only be sent if the client previously sent a resources/subscribe request.
   */
  final case class ResourceUpdatedNotification(uri: String) extends Notification:
    override def method: Method = Method.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED
  object ResourceUpdatedNotification:
    given Decoder[ResourceUpdatedNotification] = Decoder.instance { cursor =>
      for {
        method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED)
        uri <- cursor.get[String]("uri")
      } yield
        if method then ResourceUpdatedNotification(uri)
        else throw new Exception("Invalid method for ResourceUpdatedNotification")
    }

    given Encoder[ResourceUpdatedNotification] = Encoder.instance { update =>
      Json.obj(
        "method" -> update.method.asJson,
        "params" -> Json.obj(
          "uri" -> update.uri.asJson,
        )
      )
    }

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
        "method" -> list.method.asJson,
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
        "method" -> list.method.asJson,
      )
    }

  /**
   * Notification of a log message passed from server to client. If no logging/setLevel request has been sent from the client, the server MAY decide which messages to send automatically.
   */
  final case class LoggingMessageNotification(level: LoggingLevel, logger: Option[String], data: Json) extends Notification:
    override def method: Method = Method.METHOD_NOTIFICATION_MESSAGE
  object LoggingMessageNotification:
    given Decoder[LoggingMessageNotification] = Decoder.instance { cursor =>
      for {
        method <- cursor.get[Method]("method").map(_ == Method.METHOD_NOTIFICATION_MESSAGE)
        level <- cursor.get[LoggingLevel]("level")
        logger <- cursor.get[Option[String]]("logger")
        data <- cursor.get[Json]("data")
      } yield
        if method then LoggingMessageNotification(level, logger, data)
        else throw new Exception("Invalid method for LoggingMessageNotification")
    }

    given Encoder[LoggingMessageNotification] = Encoder.instance { message =>
      Json.obj(
        "method" -> message.method.asJson,
        "params" -> Json.obj(
          "level" -> message.level.asJson,
          "logger" -> message.logger.asJson,
          "data" -> message.data.asJson,
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
        "method" -> list.method.asJson,
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
      Json.obj(
        "method" -> cancel.method.asJson,
        "params" -> cancel.params.asJson,
      ).dropNullValues
    }
