import scala.language.implicitConversions

import sbt.*
import sbt.Keys.*

import sbtcrossproject.CrossProject

import de.heikoseeberger.sbtheader.AutomateHeaderPlugin

import org.typelevel.sbt.NoPublishPlugin

import BuildSettings.*
import ProjectKeys.*
import ScalaVersions.*

object Implicits {

  implicit class CrossProjectOps(private val project: CrossProject) extends AnyVal {

    def default(_name: String, projectDescription: String): CrossProject =
      project
        .in(file(_name))
        .settings(
          name        := s"${ (ThisBuild / projectName).value }-${ _name }",
          description := projectDescription
        )
        .defaultSettings

    def module(_name: String, projectDescription: String): CrossProject =
      project
        .in(file(s"module/mcp-scala-${ _name }"))
        .settings(
          name        := s"${ (ThisBuild / projectName).value }-${ _name }",
          description := projectDescription
        )
        .defaultSettings

    def example(_name: String, projectDescription: String): CrossProject =
      project
        .in(file(s"examples/${ _name }"))
        .settings(
          name        := _name,
          description := projectDescription
        )
        .defaultSettings
        .enablePlugins(NoPublishPlugin)

    def defaultSettings: CrossProject =
      project
        .settings(scalaVersion := scala3)
        .settings(scalacOptions ++= additionalSettings)
        .settings(scalacOptions --= removeSettings)
        .settings(commonSettings)
        .enablePlugins(AutomateHeaderPlugin)
  }

  implicit def builderOps(builder: CrossProject.Builder): CrossProjectOps =
    new CrossProjectOps(builder.build())
}
