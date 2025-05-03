# MCP (Model Context Protocol) for Scala 3

> [!CAUTION]
> **mcp-scala** is currently under active development. Please note that current functionality may therefore be deprecated or changed in the future.

## Usage

```scala 3
import io.circe.*
import mcp.schema.*

object StdioServer extends IOApp.Simple:
  
  case class Input(
    @Description("description for argument") argument: String
  ) derives JsonSchema, Decoder

  private val tool = Tool[IO, Input](
    "Tool Name",
    "Tool Description",
    request => IO(
      CallToolResult.success(
        Content.text(s"Hello ${request.argument}") :: Nil
      )
    )
  )

  override def run: IO[Unit] =
    McpServer
      .FastMcp[IO]("MCP Server Name", "0.1.0")
      .addTool(tool)
      .start("stdio")
```
