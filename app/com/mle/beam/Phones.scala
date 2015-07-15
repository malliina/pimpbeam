package com.mle.beam

import com.mle.play.controllers.AuthResult
import com.mle.play.ws.SyncAuth
import com.mle.util.Log
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
  with SyncAuth
  with Log {

  override def authenticate(implicit req: RequestHeader): Option[AuthResult] = PhoneSecurity.authenticate(req)

  override def openSocketCall: Call = routes.Phones.openSocket()

  def newClient(user: AuthSuccess, channel: Channel[Message])(implicit request: RequestHeader): Client =
    new BeamClient(user.user, channel)

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
}
