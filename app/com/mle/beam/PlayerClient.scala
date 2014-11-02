package com.mle.beam

import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.JsValue
import com.mle.util.Log

/**
 *
 * @author mle
 */
class PlayerClient(user: String, controlChannel: Channel[JsValue])
  extends BeamClient(user, controlChannel) with Log {

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
    controlChannel push BeamMessages.reset
  }
}