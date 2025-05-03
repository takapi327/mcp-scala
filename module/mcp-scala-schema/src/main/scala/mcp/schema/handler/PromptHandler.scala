/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema.handler

import mcp.schema.Prompt
import mcp.schema.request.GetPromptRequest
import mcp.schema.result.GetPromptResult

sealed trait PromptHandler[F[_]]:
  
  def prompt: Prompt

  def handler: GetPromptRequest => F[GetPromptResult]

object PromptHandler:
  
  private case class Impl[F[_]](
                                prompt: Prompt,
                                handler: GetPromptRequest => F[GetPromptResult]
                              ) extends PromptHandler[F]
  
  def apply[F[_]](
    prompt: Prompt,
    handler: GetPromptRequest => F[GetPromptResult]
  ): PromptHandler[F] = Impl(prompt, handler)
