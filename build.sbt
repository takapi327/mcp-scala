import BuildSettings.*
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
ThisBuild / githubWorkflowBuildPreamble ++= nativeBrewInstallWorkflowSteps.value
ThisBuild / nativeBrewInstallCond               := Some("matrix.project == 'mcpScalaNative'")
ThisBuild / githubWorkflowTargetBranches        := Seq("**")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))

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
      "org.typelevel" %%% "cats-effect"       % "3.6.0",
      "co.fs2"        %%% "fs2-core"          % "3.12.0",
      "co.fs2"        %%% "fs2-io"            % "3.12.0",
      "org.typelevel" %%% "munit-cats-effect" % "2.1.0" % Test
    )
  )
  .nativeEnablePlugins(ScalaNativeBrewedConfigPlugin)
  .nativeSettings(Test / nativeBrewFormulas += "s2n")
  .dependsOn(schema)

lazy val client = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .module("client", "Project for MCP client")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % "3.6.0",
      "co.fs2"        %%% "fs2-core"    % "3.12.0",
      "co.fs2"        %%% "fs2-io"      % "3.12.0"
    )
  )
  .dependsOn(schema)

lazy val ldbcMcpServerExample = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .example("ldbc-mcp-server", "Example project for MySQL MCP server using ldbc")
  .settings(
    run / fork := false,
    libraryDependencies += "io.github.takapi327" %%% "ldbc-connector" % "0.3.0-RC1"
  )
  .jsSettings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    Compile / mainClass := Some("StdioMain"),
  )
  .dependsOn(server)

lazy val examples = Seq(
  ldbcMcpServerExample
)

lazy val mcpScala = tlCrossRootProject
  .settings(description := "Pure functional MCP SDK with Cats Effect 3 and Scala 3")
  .settings(commonSettings)
  .aggregate(
    schema,
    server,
    client
  )
  .aggregate(examples *)
  .enablePlugins(NoPublishPlugin)
