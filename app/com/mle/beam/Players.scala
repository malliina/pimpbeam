package com.mle.beam

import com.mle.play.controllers.AuthResult
import com.mle.play.ws.SyncAuth
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.JsValue
import play.api.mvc.{Call, RequestHeader}

/**
 *
 * @author mle
 */
object Players extends Players with BeamClientStore[PlayerClient]

trait Players
  extends BeamWebSocket[PlayerClient]
  with ClientStore[PlayerClient, JsValue]
  with SyncAuth {

  def newClient(user: AuthSuccess, channel: Channel[Message])(implicit request: RequestHeader): Client =
    new PlayerClient(user.user, channel)

  //  def wsUrl(implicit request: RequestHeader): String = {
  //    // Cannot use routes.Home.wsplay().webSocketURL(Boolean) because:
  //    // a) When using Azure, the host and port would be the one Play
  //    //    runs on internally, not the desired external one.
  //    // b) We don't know whether the request is made over HTTPS or HTTP.
  //
  //    // Serves wss if it's available, regardless of what the browser request was
  //    val server = PlayStarter.serverUtils
  //    val secured = server.exists(_.isHttpsAvailable)
  //    val (protocol, port) =
  //      if (secured) ("wss", Home.beamConf.sslPort)
  //      else ("ws", Home.beamConf.port)
  //    val domain = request.domain
  //    val actualPort = if (domain == "localhost" && server.isEmpty) 9000 else port
  //    val url = routes.Home.wsplay().url
  //    s"$protocol://$domain:$actualPort$url"
  //  }

  override def openSocketCall: Call = routes.Players.openSocket()

  override def authenticate(implicit request: RequestHeader): Option[AuthResult] =
    PlayerSecurity.authenticate(request)

  /**
   *
   * @return the player connected as the username specified in the session
   */
  def auth(implicit request: RequestHeader): Option[PlayerClient] =
    authenticate(request) flatMap (u => client(u.user))

  override def onMessage(msg: Message, client: Client): Boolean = {
    log debug s"Got msg: $msg"
    Phones.usercast(client.user, msg)
    true
  }

  override def onDisconnect(client: PlayerClient) {
    super.onDisconnect(client)
    // informs the music source that the player has disconnected
    val user = client.user
    Phones.usercast(user, BeamMessages.partyDisconnected(user))
  }
}
