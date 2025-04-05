/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package ldbc.mcp

import cats.effect.*

import mcp.server.McpServer

object StdioMain extends IOApp.Simple:

  override def run: IO[Unit] =
    McpServer
      .FastMcp[IO]("MySQL Server", "0.1.0")
      .addTool(MySQLTool.tool)
      .start("stdio")
