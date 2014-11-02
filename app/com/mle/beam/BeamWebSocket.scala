package com.mle.beam

import com.mle.play.controllers.AuthResult
import com.mle.play.json.JsonMessages
import com.mle.play.ws.JsonWebSockets
import com.mle.util.Log

/**
 *
 * @author mle
 */
trait BeamWebSocket[T <: BeamClient] extends JsonWebSockets with BeamClientStore[T] with Log {
  type AuthSuccess = AuthResult
  type Client = T

  override def welcomeMessage(client: Client) = Some(JsonMessages.welcome)

  /**
   * Called when a client has connected.
   *
   * @param client the client channel, can be used to push messages to the client
   */
  def onConnect(client: T) {
    val user = client.user
    add(user, client)
    log debug s"Opened WebSocket for user: $user"
  }

  /**
   * Called when a client has disconnected.
   *
   * @param client the disconnected client channel
   */
  def onDisconnect(client: T) {
    val user = client.user
    remove(user)
    log debug s"Closed WebSocket for user: $user"
  }
}
