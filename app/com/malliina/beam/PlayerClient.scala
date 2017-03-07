package com.malliina.beam

import akka.stream.scaladsl.SourceQueue
import com.malliina.beam.PlayerClient.log
import com.malliina.play.models.Username
import play.api.Logger
import play.api.libs.json.JsValue

object PlayerClient {
  private val log = Logger(getClass)
}

class PlayerClient(user: Username, channel: SourceQueue[JsValue])
  extends BeamClient(user, channel) {

  @volatile
  var streamer = StreamManager.empty()

  log debug s"Created stream for user: $user"

  def stream = streamer.chunkedStream

  def audioChannel = streamer.channel

  /**
    * Ends the current stream and replaces it with a new one, then sends a reset message to the client so that the client
    * will receive the newly created stream instead.
    *
    * This is used when the user starts playing a new track, discarding any currently playing stream.
    */
  def resetStream(): Unit = {
    streamer.eofAndEnd()
    streamer = StreamManager.empty()
    // instructs the client to GET /stream
    channel offer BeamMessages.reset
  }
}
