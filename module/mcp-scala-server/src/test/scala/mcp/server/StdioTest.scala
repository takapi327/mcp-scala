/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.server

import io.circe.*

import cats.effect.*

import munit.*

import mcp.schema.McpSchema

class StdioTest extends CatsEffectSuite:
  
  case class RandomNumber(min: Int, max: Int)
  object RandomNumber:
    given Encoder[RandomNumber] = Encoder.derived
    given Decoder[RandomNumber] = Decoder.derived

  def tool: McpSchema.Tool[IO, RandomNumber] = McpSchema.Tool(
    "RandomNumber",
    "Generate a random number between min and max",
    McpSchema.JsonSchema(
      "object",
      Map(
        "min" -> Json.obj("type" -> Json.fromString("number")),
        "max" -> Json.obj("type" -> Json.fromString("number"))
      ),
      List("min", "max"),
      false
    ),
    request => IO.pure {
      val random = scala.util.Random.between(request.min, request.max)
      val contents = List(McpSchema.Content.text(random.toString))
      McpSchema.CallToolResult.success(contents)
    }
  )
  
  test("test") {
    //val json = RandomNumber(1, 10).asJson
    //val server = McpServer[IO]("MCP Server", "0.1.0")
    //  .addTool(tool)
    //  .start("stdio")
  }
  