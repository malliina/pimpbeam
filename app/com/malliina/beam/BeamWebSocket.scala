package com.malliina.beam

import akka.stream.Materializer
import com.malliina.beam.BeamWebSocket.log
import com.malliina.play.http.AuthedRequest
import com.malliina.play.json.JsonMessages
import com.malliina.play.ws.JsonWebSockets
import play.api.Logger

import scala.concurrent.Future

object BeamWebSocket {
  private val log = Logger(getClass)
}

abstract class BeamWebSocket[T <: BeamClient](val connections: BeamClientStore[T], mat: Materializer)
  extends JsonWebSockets(mat) {

  type AuthSuccess = AuthedRequest
  type Client = T

  override def clients = Future.successful(connections.clients)

  override def welcomeMessage(client: Client) = Some(JsonMessages.welcome)

  /** Called when a client has connected.
    *
    * @param client the client channel, can be used to push messages to the client
    */
  override def onConnect(client: T) = Future.successful {
    val user = client.user
    connections.add(user, client)
    log debug s"Opened WebSocket for user: $user"
  }

  /** Called when a client has disconnected.
    *
    * @param client the disconnected client channel
    */
  def onDisconnect(client: T) = Future.successful {
    val user = client.user
    connections.remove(user)
    log debug s"Closed WebSocket for user: $user"
  }
}
