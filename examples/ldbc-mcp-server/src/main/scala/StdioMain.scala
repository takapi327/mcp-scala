import cats.effect.*

import mcp.server.McpServer

object StdioMain extends IOApp.Simple:

  override def run: IO[Unit] =
    McpServer
      .FastMcp[IO]("MySQL Server", "0.1.0")
      .addTool(MySQLTool.tool)
      .start("stdio")
