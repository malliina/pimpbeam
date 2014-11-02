package com.mle.beam

import play.api.libs.iteratee._
import com.mle.util.Log
import java.util.UUID

/**
 * http://greweb.me/2012/08/zound-a-playframework-2-audio-streaming-experiment-using-iteratees/
 *
 * @author mle
 */
class StreamManager(val rawStream: Enumerator[Array[Byte]], val channel: Concurrent.Channel[Array[Byte]]) extends Log {
  @volatile
  var isReceivingStream: Boolean = false
  // Not sure how helpful this is but it does not seem to break things
  // The intention is to reduce the number of HTTP chunks sent per second
  private val chunker = Enumeratee.grouped(
    Traversable.take[Array[Byte]](5000) transform Iteratee.consume[Array[Byte]]()
  )
  val chunkedStream = rawStream through chunker
  // Not sure how sharedChunkedStream is better than chunkedStream but
  // using it does not seem to break things
  //  val (sharedChunkedStream, _) = Concurrent.broadcast(chunkedStream)

  def eofAndEnd(): Unit = channel.eofAndEnd()
}

object StreamManager {
  def empty() = {
    val (rawStream, channel) = Concurrent.broadcast[Array[Byte]]
    fromStream(rawStream, channel)
  }

  def fromStream(rawStream: Enumerator[Array[Byte]], channel: Concurrent.Channel[Array[Byte]]) =
    new StreamManager(rawStream, channel)
}