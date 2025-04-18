/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

import java.math.{ BigDecimal as JBigDecimal, BigInteger as JBigInteger }
import java.time.*
import java.util.{ Date, UUID }

import scala.annotation.StaticAnnotation
import scala.concurrent.duration.Duration as ScalaDuration
import scala.quoted.*

import io.circe.*
import io.circe.syntax.*

import SchemaType.*

trait JsonSchema[A]:
  def schemaType:  SchemaType[A]
  def title:       Option[String]
  def description: Option[String]
  def isOptional:  Boolean

  def thisType: String

  /** Returns an optional version of this schema, with `isOptional` set to true. */
  def asOption: JsonSchema[Option[A]]

  /** Returns an array version of this schema, with the schema type wrapped in [[SchemaType.SArray]]. Sets `isOptional`
   * to true as the collection might be empty.
   */
  def asArray: JsonSchema[Array[A]]

  /** Returns a collection version of this schema, with the schema type wrapped in [[SchemaType.SArray]]. Sets
   * `isOptional` to true as the collection might be empty.
   */
  def asIterable[C[X] <: Iterable[X]]: JsonSchema[C[A]]

case class Description(name: String) extends StaticAnnotation

object JsonSchema:

  given [A]: Encoder[JsonSchema[A]] = Encoder.instance { schema =>
    schema.schemaType match
      case entity: Entity[?] =>
        Json
          .obj(
            "type" -> Json.fromString(entity.toString),
            "properties" -> Json.obj(
              entity.fields.map { field =>
                field.name -> field.schema.schemaType.asJson
                  .deepMerge(
                    Json.obj("description" -> field.description.asJson)
                  )
                  .dropNullValues
                  .dropEmptyValues
              }*
            ),
            "required" -> Json.arr(
              entity.required.map(Json.fromString)*
            ),
            "additionalProperties" -> Json.fromBoolean(false)
          )
          .dropNullValues
          .dropEmptyValues
      case other =>
        Json
          .obj(
            "type"        -> Json.fromString(other.toString),
            "title"       -> schema.title.asJson,
            "description" -> schema.description.asJson,
            "properties"  -> Json.obj(),
            "required"    -> Json.arr(),
            "isOptional"  -> Json.fromBoolean(schema.isOptional)
          )
          .dropNullValues
          .dropEmptyValues
  }

  def apply[A](schemaType: SchemaType[A]): JsonSchema[A] =
    Impl(schemaType, None, None, false)

  def apply[A](schemaType: SchemaType[A], format: Option[String]): JsonSchema[A] =
    Impl(schemaType, None, format, false)

  def apply[A](schemaType: SchemaType[A], isOptional: Boolean): JsonSchema[A] =
    Impl(schemaType, None, None, isOptional)

  def apply[A](
    schemaType: SchemaType[A],
    title:      Option[String],
    format:     Option[String],
    isOptional: Boolean
  ): JsonSchema[A] = Impl(schemaType, title, format, isOptional)

  private case class Impl[A](
    schemaType:  SchemaType[A],
    title:       Option[String],
    description: Option[String],
    isOptional:  Boolean
  ) extends JsonSchema[A]:
    override def thisType: String = schemaType.toString

    override def asOption: JsonSchema[Option[A]] = copy(schemaType = SOption(this), isOptional = true)

    override def asArray: JsonSchema[Array[A]] = copy(schemaType = SArray(this), isOptional = true)

    override def asIterable[C[X] <: Iterable[X]]: JsonSchema[C[A]] =
      copy(schemaType = SArray(this), isOptional = true)

  given JsonSchema[String]         = JsonSchema(SString())
  given JsonSchema[Byte]           = JsonSchema(SInteger())
  given JsonSchema[Short]          = JsonSchema(SInteger())
  given JsonSchema[Int]            = JsonSchema(SInteger[Int]())
  given JsonSchema[Long]           = JsonSchema(SInteger[Long]())
  given JsonSchema[Float]          = JsonSchema(SNumber[Float]())
  given JsonSchema[Double]         = JsonSchema(SNumber[Double]())
  given JsonSchema[Boolean]        = JsonSchema(SBoolean())
  given JsonSchema[Array[Byte]]    = JsonSchema(SBinary())
  given JsonSchema[Instant]        = JsonSchema(SBinary())
  given JsonSchema[ZonedDateTime]  = JsonSchema(SDateTime())
  given JsonSchema[OffsetDateTime] = JsonSchema(SDateTime())
  given JsonSchema[Date]           = JsonSchema(SDate())
  given JsonSchema[LocalDateTime]  = JsonSchema(SString())
  given JsonSchema[LocalDate]      = JsonSchema(SDate())
  given JsonSchema[ZoneOffset]     = JsonSchema(SString())
  given JsonSchema[Duration]       = JsonSchema(SString())
  given JsonSchema[LocalTime]      = JsonSchema(SString())
  given JsonSchema[OffsetTime]     = JsonSchema(SString())
  given JsonSchema[ScalaDuration]  = JsonSchema(SString())
  given JsonSchema[UUID]           = JsonSchema(SString[UUID]())
  given JsonSchema[BigDecimal]     = JsonSchema(SNumber())
  given JsonSchema[JBigDecimal]    = JsonSchema(SNumber())
  given JsonSchema[BigInt]         = JsonSchema(SInteger())
  given JsonSchema[JBigInteger]    = JsonSchema(SInteger())
  given JsonSchema[Unit]           = JsonSchema(Entity.empty)

  given [A: JsonSchema]:                      JsonSchema[Option[A]] = summon[JsonSchema[A]].asOption
  given [A: JsonSchema]:                      JsonSchema[Array[A]]  = summon[JsonSchema[A]].asArray
  given [A: JsonSchema, C[X] <: Iterable[X]]: JsonSchema[C[A]]      = summon[JsonSchema[A]].asIterable[C]

  inline def derived[P <: Product]: JsonSchema[P] = ${ derivedImpl[P] }

  private def derivedImpl[P <: Product](using
    quotes: Quotes,
    tpe:    Type[P]
  ): Expr[JsonSchema[P]] =

    import quotes.reflect.*

    val symbol = TypeRepr.of[P].typeSymbol

    val annot = TypeRepr.of[Description].typeSymbol

    val labels = symbol.primaryConstructor.paramSymss.flatten
      .collect {
        case sym if sym.hasAnnotation(annot) =>
          val annotExpr = sym.getAnnotation(annot).get.asExprOf[Description]
          val name      = Expr(sym.name)
          '{ ($name, Some($annotExpr.name)) }
        case sym =>
          val name = Expr(sym.name)
          '{ ($name, None) }
      }

    val schemas = Expr.ofSeq(
      symbol.caseFields
        .map { field =>
          field.tree match
            case ValDef(name, tpt, _) =>
              tpt.tpe.asType match
                case '[tpe] =>
                  Expr.summon[JsonSchema[tpe]].getOrElse {
                    report.errorAndAbort(s"Json Schema for type $tpe not found")
                  }
                case _ =>
                  report.errorAndAbort(s"Type $tpt is not a type")
        }
    )

    val fields = '{
      ${ Expr.ofSeq(labels) }
        .zip($schemas)
        .map {
          case (label: (String, Option[String]), schema: JsonSchema[t]) =>
            Entity.Field[t](
              _name        = label._1,
              _schema      = schema,
              _description = label._2
            )
        }
        .toList
    }

    '{
      JsonSchema[P](
        schemaType = Entity($fields),
        title      = Some(${ Expr(symbol.name) }),
        format     = None,
        isOptional = false
      )
    }
