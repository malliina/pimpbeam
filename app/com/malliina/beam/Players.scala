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
}
