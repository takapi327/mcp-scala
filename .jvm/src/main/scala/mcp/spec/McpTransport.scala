package mcp.spec

import mcp.spec.McpSchema.JSONRPCMessage

/**
 * Model Context Protocol (MCP)の非同期トランスポート層を定義します。
 *
 * McpTransportトレイトはModel Context Protocolでカスタムトランスポート
 * メカニズムを実装するための基盤を提供します。クライアントとサーバーコンポーネント間の
 * 双方向通信を処理し、JSON-RPC形式を使用した非同期メッセージ交換をサポートします。
 *
 * このトレイトの実装は以下の責務を持ちます：
 * - トランスポート接続のライフサイクル管理
 * - サーバーからの受信メッセージとエラーの処理
 * - サーバーへの送信メッセージの処理
 *
 * トランスポート層はプロトコルに依存しない設計になっており、WebSocket、HTTP、
 * またはカスタムプロトコルなど、さまざまな実装が可能です。
 */
trait McpTransport[F[_]]:

  /**
   * トランスポート接続を非同期に閉じ、関連するリソースを解放します。
   * @return 接続が閉じられたときに完了するF[Unit]
   */
  def close(): F[Unit]

  /**
   * メッセージをピアに非同期で送信します。
   *
   * このメソッドは、非同期方式でサーバーへのメッセージの送信を処理します。
   * メッセージはMCPプロトコルで指定されたJSON-RPC形式で送信されます。
   *
   * @param message サーバーに送信されるJSONRPCMessage
   * @return メッセージが送信されたときに完了するF[Unit]
   */
  def sendMessage(message: JSONRPCMessage): F[Unit]

  /**
   * 指定されたデータを指定された型のオブジェクトにアンマーシャルします。
   * @param data アンマーシャルするデータ
   * @tparam T アンマーシャルするオブジェクトの型
   * @return アンマーシャルされたオブジェクト
   */
  def unmarshalFrom[T](data: Any): T
