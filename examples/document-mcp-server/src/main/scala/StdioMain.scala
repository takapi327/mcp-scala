/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

import cats.effect.*

import fs2.io.*

import mcp.schema.McpSchema
import mcp.server.McpServer

object StdioMain extends IOApp.Simple:

  private val resourceHandler = new McpSchema.ResourceHandler[IO]:
    override def resource: McpSchema.Resource = McpSchema.Resource(
      "/Users/takapi327/Development/oss/typelevel/affiliate/ldbc/README.md",
      "ldbc documentation",
      "ldbc documentation",
      "text/markdown",
      McpSchema.Annotations(List.empty, None)
    )
    override def readHandler: McpSchema.ReadResourceRequest => IO[McpSchema.ReadResourceResult] =
      request =>
        file.Files[IO]
          .readUtf8(file.Path(resource.uri))
          .compile
          .toList
          .map { contents =>
            McpSchema.ReadResourceResult(
              contents.map(content => McpSchema.TextResourceContents(request.uri, "text/markdown", content))
            )
          }

  override def run: IO[Unit] =
    McpServer
      .FastMcp[IO]("Documentation Server", "0.1.0")
      .setCapabilities(McpSchema.ServerCapabilities(
        McpSchema.ResourceCapabilities(None, None),
        McpSchema.ToolCapabilities(false)
      ))
      .addResource(resourceHandler)
      .start("stdio")
