package com.malliina.beam

import akka.stream.Materializer
import akka.stream.scaladsl.SourceQueue
import com.malliina.beam.Players.log
import com.malliina.play.http.AuthedRequest
import com.malliina.play.models.Username
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc.{Call, RequestHeader}

import scala.concurrent.Future

object Players {
  private val log = Logger(getClass)
}

class Players(auth: PlayerSecurity,
              playerClients: BeamClientStore[PlayerClient],
              phones: BeamClientStore[BeamClient],
              mat: Materializer)
  extends BeamWebSocket[PlayerClient](playerClients, mat) {

  override def newClient(authResult: AuthedRequest, channel: SourceQueue[JsValue], request: RequestHeader) =
    new PlayerClient(authResult.user, channel)

  override def openSocketCall: Call = routes.Players.openSocket()

  override def authenticateAsync(rh: RequestHeader): Future[AuthedRequest] =
    authUser(rh).flatMap { maybe =>
      maybe
        .map(user => Future.successful(user))
        .getOrElse(Future.failed(new NoSuchElementException))
    }

  def authUser(rh: RequestHeader) = auth.authenticate(rh)

  /**
    * @return the player connected as the username specified in the session
    */
  def auth(rh: RequestHeader): Future[Option[PlayerClient]] =
    authenticateAsync(rh) map { user => connections.client(user.user) }

  override def onMessage(msg: Message, client: Client): Boolean = {
    log debug s"Got msg: $msg"
    phones.usercast(client.user, msg)
    true
  }

  override def onDisconnect(client: PlayerClient): Future[Unit] =
    super.onDisconnect(client).map { _ =>
      // informs the music source that the player has disconnected
      val user = client.user
      phones.usercast(user, BeamMessages.partyDisconnected(user))
    }

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
}
