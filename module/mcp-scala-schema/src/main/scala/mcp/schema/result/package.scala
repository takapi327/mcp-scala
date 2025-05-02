/**
 * Copyright (c) 2025-2026 by Takahiko Tominaga
 * This software is licensed under the MIT License (MIT).
 * For more information see LICENSE or https://opensource.org/licenses/MIT
 */

package mcp.schema

package object result:

  trait Result
  
  object Result:

    /**
     * A response that indicates success but carries no data.
     */
    final case class Empty() extends Result

  trait PaginatedResult extends Result:
    /**
     * An opaque token representing the pagination position after the last returned result.
     * If present, there may be more results available.
     */
    def nextCursor: Option[Cursor]
