import Implicits.*
import JavaVersions.*
import ProjectKeys.*
import ScalaVersions.*

ThisBuild / tlBaseVersion      := "0.1"
ThisBuild / tlFatalWarnings    := true
ThisBuild / projectName        := "mcp-scala"
ThisBuild / scalaVersion       := scala3
ThisBuild / crossScalaVersions := Seq(scala3, scala36)
ThisBuild / githubWorkflowJavaVersions := Seq(
  JavaSpec.corretto(java11),
  JavaSpec.corretto(java17),
  JavaSpec.corretto(java21)
)
ThisBuild / githubWorkflowTargetBranches        := Seq("**")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / tlSitePublishBranch                 := None

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository                 := "https://s01.oss.sonatype.org/service/local"

lazy val schema = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .module("schema", "Project for MCP schema")
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core"    % "0.14.8",
      "io.circe" %%% "circe-generic" % "0.14.8",
      "io.circe" %%% "circe-parser"  % "0.14.8"
    )
  )

lazy val server = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .module("server", "Project for MCP server")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect"         % "3.6.0",
      "co.fs2"        %%% "fs2-core"            % "3.12.0",
      "co.fs2"        %%% "fs2-io"              % "3.12.0",
      "org.http4s"    %%% "http4s-ember-client" % "0.23.30",
      "org.http4s"    %%% "http4s-ember-server" % "0.23.30",
      "org.http4s"    %%% "http4s-dsl"          % "0.23.30"
    )
  )
  .dependsOn(schema)

lazy val client = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .module("client", "Project for MCP client")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect"         % "3.6.0",
      "co.fs2"        %%% "fs2-core"            % "3.12.0",
      "co.fs2"        %%% "fs2-io"              % "3.12.0",
      "org.http4s"    %%% "http4s-ember-client" % "0.23.30",
      "org.http4s"    %%% "http4s-ember-server" % "0.23.30",
      "org.http4s"    %%% "http4s-dsl"          % "0.23.30"
    )
  )
  .dependsOn(schema)

lazy val mcpScala = tlCrossRootProject
  .settings(description := "Pure functional MCP SDK with Cats Effect 3 and Scala 3")
  .aggregate(
    schema,
    server,
    client
  )
  .enablePlugins(NoPublishPlugin)
