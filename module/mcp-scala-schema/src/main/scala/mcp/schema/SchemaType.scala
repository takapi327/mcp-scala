/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import io.circe.*
import io.circe.syntax.*

sealed trait SchemaType[A](`type`: String):
  type ThisType = A
  override def toString: String = `type`

object SchemaType:

  given [A]: Encoder[SchemaType[A]] = Encoder.instance {
    case SString() => Json.obj("type" -> Json.fromString("string"))
    case SInteger() => Json.obj("type" -> Json.fromString("number"))
    case SNumber() => Json.obj("type" -> Json.fromString("number"))
    case SBoolean() => Json.obj("type" -> Json.fromString("boolean"))
    case SBinary() => Json.obj("type" -> Json.fromString("binary"))
    case SDate() => Json.obj("type" -> Json.fromString("date"))
    case SDateTime() => Json.obj("type" -> Json.fromString("date-time"))
    case SOption(element) => element.asJson
    case SArray(element) => Json.obj("type" -> Json.fromString("array"), "items" -> Json.obj(
      "properties" -> element.asJson,
    ))
    case Entity(fields) =>
      Json.obj(
        "type" -> Json.fromString("object"),
        "properties" -> Json.obj(
          fields.map { field =>
            field.name -> field.schema.schemaType.asJson
          }*,
        ),
        "required" -> Json.arr(
          fields.collect {
            case field if !field.schema.isOptional => Json.fromString(field.name)
          }*,
        ),
      )
  }

  final case class SString[A]() extends SchemaType[A]("string")
  final case class SInteger[A]() extends SchemaType[A]("integer")
  final case class SNumber[A]() extends SchemaType[A]("number")
  final case class SBoolean[A]() extends SchemaType[A]("boolean")
  final case class SBinary[A]() extends SchemaType[A]("binary")
  final case class SDate[A]() extends SchemaType[A]("date")
  final case class SDateTime[A]() extends SchemaType[A]("date-time")

  final case class SOption[T, S](element: JsonSchema[S]) extends SchemaType[T](element.thisType)
  final case class SArray[T, S](element: JsonSchema[S]) extends SchemaType[T](element.thisType)

  final case class Entity[A](fields: List[Entity.Field]) extends SchemaType[A]("object"):
    def required: List[String] = fields.collect {
      case field if !field.schema.isOptional => field.name
    }

  object Entity:

    trait Field:
      type FieldType
      def name: String
      def schema: JsonSchema[FieldType]
      def description: Option[String] = None

    object Field:
      def apply[A](_name: String, _schema: JsonSchema[A], _description: Option[String] = None): Field = new Field:
        type FieldType = A
        override def name: String = _name
        override def schema: JsonSchema[FieldType] = _schema
        override def description: Option[String] = _description
    
    def empty[A]: Entity[A] = Entity(Nil)
