/**
 *  This file is part of the ldbc.
 *  For the full copyright and license information,
 *  please view the LICENSE file that was distributed with this source code.
 */

import sbt.*
import sbt.Keys.*
import sbt.ScriptedPlugin.autoImport.*

import de.heikoseeberger.sbtheader.{ CommentBlockCreator, CommentStyle }
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.*
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.HeaderPattern.commentBetween

import org.typelevel.sbt.TypelevelGitHubPlugin.autoImport.tlGitHubDev

object BuildSettings {

  val additionalSettings: Seq[String] = Seq(
    "-language:implicitConversions"
  )

  val removeSettings: Seq[String] = Seq(
    "-Ykind-projector:underscores",
    "-Wvalue-discard"
  )

  /**
   * Set up a scripted framework to test the plugin.
   */
  def scriptedSettings: Seq[Setting[?]] = Seq(
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )

  val customCommentStyle: CommentStyle =
    CommentStyle(
      new CommentBlockCreator("/**", " *", " */"),
      commentBetween("""/\**+""", "*", """\*/""")
    )

  /** These settings are used by all projects. */
  def commonSettings: Seq[Setting[?]] = Def.settings(
    organization     := "io.github.takapi327",
    organizationName := "takapi327",
    startYear        := Some(2025),
    homepage         := Some(url("https://takapi327.github.io/mcp-scala/")),
    licenses         := Seq("MIT" -> url("https://img.shields.io/badge/license-MIT-green")),
    run / fork       := true,
    developers += tlGitHubDev("takapi327", "Takahiko Tominaga"),
    headerMappings := headerMappings.value + (HeaderFileType.scala -> customCommentStyle),
    headerLicense := Some(
      HeaderLicense.Custom(
        """|Copyright (c) 2023-2024 by Takahiko Tominaga
         |This software is licensed under the MIT License (MIT).
         |For more information see LICENSE or https://opensource.org/licenses/MIT
         |""".stripMargin
      )
    )
  )
}
