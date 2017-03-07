package com.malliina.beam

import akka.stream.Materializer
import akka.stream.scaladsl.SourceQueue
import com.malliina.beam.Phones.log
import com.malliina.play.http.AuthedRequest
import com.malliina.play.models.Username
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc.{Call, RequestHeader}

import scala.concurrent.Future

object Phones {
  private val log = Logger(getClass)
}

class Phones(auth: PhoneSecurity,
             clients: BeamClientStore[BeamClient],
             players: BeamClientStore[PlayerClient],
             mat: Materializer)
  extends BeamWebSocket[BeamClient](clients, mat) {

  override def authenticateAsync(req: RequestHeader): Future[AuthedRequest] =
    auth.authenticate(req).flatMap { maybe =>
      maybe
        .map(Future.successful)
        .getOrElse(Future.failed(new NoSuchElementException))
    }

  override def openSocketCall: Call = routes.Phones.openSocket()

  override def newClient(authResult: AuthedRequest, channel: SourceQueue[JsValue], request: RequestHeader) =
    new BeamClient(authResult.user, channel)

  override def onMessage(msg: Message, client: Client): Boolean = {
    log info s"Got msg: $msg"
    // routes the message to the player with the same userid
    players.usercast(client.user, msg)
    true
  }

  override def onDisconnect(client: BeamClient): Future[Unit] = {
    super.onDisconnect(client) map { _ =>
      val user = client.user
      // informs the browser that the mobile device has disconnected
      players.usercast(user, BeamMessages.partyDisconnected(user))
    }
  }
}
