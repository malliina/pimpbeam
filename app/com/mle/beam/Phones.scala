package com.mle.beam

import com.mle.play.PlaySecurity
import com.mle.play.auth.BasicCredentials
import com.mle.play.controllers.AuthResult
import com.mle.play.ws.SyncAuth
import com.mle.util.Log
import controllers.Home
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.JsValue
import play.api.mvc.{Call, RequestHeader}

/**
 *
 * @author mle
 */
object Phones extends Phones with BeamClientStore[BeamClient]

trait Phones
  extends BeamWebSocket[BeamClient]
  with ClientStore[BeamClient, JsValue]
  with PlaySecurity
  with SyncAuth
  with Log {

  override def openSocketCall: Call = routes.Phones.openSocket()

  def newClient(user: AuthSuccess, channel: Channel[Message])(implicit request: RequestHeader): Client =
    new BeamClient(user.user, channel)

  //  def wsUrl(implicit request: RequestHeader): String = routes.Home.control().webSocketURL(request.secure)

  override def authenticate(implicit request: RequestHeader): Option[AuthResult] =
    authenticateFromHeader.map(u => AuthResult(u))

  override def onMessage(msg: Message, client: Client): Boolean = {
    log info s"Got msg: $msg"
    // routes the message to the player with the same userid
    Players.usercast(client.user, msg)
    true
  }

  override def onDisconnect(client: BeamClient): Unit = {
    super.onDisconnect(client)
    val user = client.user
    // informs the browser that the mobile device has disconnected
    Players.usercast(user, BeamMessages.partyDisconnected(user))
  }

  /**
   * Validates the supplied credentials, which are valid if:
   *
   * 1) a player is connected with the same non-empty username as the one supplied here
   * 2) the default password is correct
   *
   * @return true if the credentials are valid, false otherwise
   */
  override def validateCredentials(creds: BasicCredentials): Boolean = {
    val user = creds.username
    log debug s"Validating: $user"
    Home.validateCredentials(creds) && ensurePlayerExists(user)
  }

  private def ensurePlayerExists(username: String): Boolean = {
    val playerExists = (Players client username).nonEmpty
    if (!playerExists) {
      log warn s"Unauthorized connection attempt as user: $username. No such player is connected."
    }
    playerExists
  }
}


