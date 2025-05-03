/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

package object request:

  trait Request:

    def method: Method

  trait PaginatedRequest extends Request:

    def cursor: Option[Cursor]
