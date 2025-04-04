package mcp.spec

import io.circe.Json

/**
 * Model Control Protocol (MCP) セッションを表します。クライアントとサーバー間の通信を処理します。
 * このトレイトはリクエストと通知の送信、およびセッションのライフサイクル管理のためのメソッドを提供します。
 *
 * セッションは非同期で動作し、ブロッキングなしの操作のためにcats-effectの`IO`型を使用します。
 * リクエスト-レスポンスパターンとワンウェイ通知の両方をサポートします。
 */
trait McpSession[F[_]]:

  /**
   * モデルカウンターパーティにリクエストを送信し、型Tのレスポンスを期待します。
   *
   * このメソッドは、クライアントまたはサーバーからのレスポンスが期待される
   * リクエスト-レスポンスパターンを処理します。
   *
   * @tparam T 期待されるレスポンスの型
   * @param method カウンターパーティで呼び出されるメソッドの名前
   * @param requestParams リクエストと共に送信されるパラメータ
   * @return レスポンスを発行するIO
   */
  def sendRequest[T](method: String, requestParams: Json): F[T]

  /**
   * パラメータなしでモデルクライアントまたはサーバーに通知を送信します。
   *
   * このメソッドはカウンターパーティからのレスポンスが期待されない通知パターンを実装します。
   * fire-and-forgetシナリオに有用です。
   *
   * @param method サーバーで呼び出される通知メソッドの名前
   * @return 通知が送信されたときに完了するIO
   */
  def sendNotification(method: String): F[Unit] = sendNotification(method, None)

  /**
   * パラメータ付きでモデルクライアントまたはサーバーに通知を送信します。
   *
   * `sendNotification(String)`と同様ですが、通知と共に追加パラメータを送信できます。
   *
   * @param method カウンターパーティに送信される通知メソッドの名前
   * @param params 通知と共に送信されるパラメータのマップ
   * @return 通知が送信されたときに完了するIO
   */
  def sendNotification(method: String, params: Option[Map[String, Any]]): F[Unit]

  /**
   * セッションを非同期に閉じ、関連するリソースを解放します。
   * @return セッションが閉じられたときに完了するIO[Unit]
   */
  def closeGracefully(): F[Unit]

  /**
   * セッションを閉じ、関連するリソースを解放します。
   * このメソッドは同期的に実行されるため、リソースの解放が完了するまでブロックします。
   */
  def close(): Unit
