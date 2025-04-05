import cats.effect.*

import io.circe.*

import mcp.schema.McpSchema

import ldbc.connector.*

case class MySQLTool(sql: String)
object MySQLTool:
  given Decoder[MySQLTool] = Decoder.derived[MySQLTool]
  given Encoder[MySQLTool] = Encoder.derived[MySQLTool]

  private def provider = ConnectionProvider
    .default[IO]("127.0.0.1", 13306, "ldbc", "password", "world")
    .setSSL(SSL.Trusted)

  def tool: McpSchema.Tool[IO, MySQLTool] = McpSchema.Tool[IO, MySQLTool](
    "MySQL",
    "MySQL Connector",
    McpSchema.JsonSchema(
      "object",
      Map(
        "sql" -> Json.obj("type" -> Json.fromString("string"))
      ),
      List("sql"),
      false
    ),
    request =>
      provider.use { connection =>
        for
          statement <- connection.createStatement()
          resultSet <- statement.executeQuery(request.sql)
        yield
          val impl    = resultSet.asInstanceOf[ResultSetImpl]
          val columns = impl.columns.map(column => s"${ column.name }: (${ column.columnType.name })")
          val records = impl.records.map(_.values)
          val contents = List(
            McpSchema.Content.text(
              s"Columns: ${ columns.mkString(", ") }\nRecords: ${ records.map(_.mkString(", ")).mkString("\n") }"
            )
          )
          McpSchema.CallToolResult.success(contents)
      }
  )
