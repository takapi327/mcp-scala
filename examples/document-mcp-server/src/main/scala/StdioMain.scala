/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

import cats.effect.*

import fs2.io.*

import mcp.schema.*
import mcp.schema.request.*
import mcp.schema.result.*

import mcp.server.McpServer

object StdioMain extends IOApp.Simple:

  private val resourceHandler = new McpSchema.ResourceHandler[IO]:
    override def resource: McpResource.Static = McpResource.static(
      "/Users/takapi327/Development/oss/typelevel/affiliate/ldbc/README.md",
      "ldbc documentation",
      None,
      None,
      Annotations(List.empty, None)
    )
    override def readHandler: ReadResourceRequest => IO[ReadResourceResult] =
      request =>
        file
          .Files[IO]
          .readUtf8(file.Path(request.uri))
          .compile
          .toList
          .map { contents =>
            ReadResourceResult(
              contents.map(content => ResourceContents.Text(request.uri, "text/markdown", content))
            )
          }

  private val resourceTemplateHandler = new McpSchema.ResourceHandler[IO]:
    override def resource: McpResource.Template = McpResource.template(
      "/Users/takapi327/Development/oss/typelevel/affiliate/ldbc/{path}",
      "ldbc Project Files",
      Some("Access files in the ldbc project directory"),
      None,
      Annotations(List.empty, None)
    )

    override def readHandler: ReadResourceRequest => IO[ReadResourceResult] =
      request =>
        file
          .Files[IO]
          .readUtf8(file.Path(request.uri))
          .compile
          .toList
          .map { contents =>
            ReadResourceResult(
              contents.map(content => ResourceContents.Text(request.uri, "text/markdown", content))
            )
          }

  private val prompt = Prompt(
    "Code Review",
    "Please review the code and provide feedback.",
    List(
      PromptArgument(
        "code",
        "The code to review",
        true
      )
    )
  )

  private val promptHandler = McpSchema.PromptHandler(
    prompt,
    request => {
      val codeOpt = request.arguments.flatMap(_.get("code")).flatMap(_.as[String].toOption)
      codeOpt match
        case None => IO.raiseError(new Exception("Code argument is required"))
        case Some(code) =>
          val content = Content.text(s"Please review this Scala code:\n\n$code")
          val result = GetPromptResult(
            Some("Code review prompt"),
            List(
              McpSchema.PromptMessage(
                Role.USER,
                content
              )
            )
          )
          IO.pure(result)
    }
  )

  override def run: IO[Unit] =
    McpServer
      .FastMcp[IO]("Documentation Server", "0.1.0")
      .addResource(resourceHandler)
      .addResource(resourceTemplateHandler)
      .addPrompt(promptHandler)
      .start("stdio")
