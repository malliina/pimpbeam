package com.malliina.beam

import akka.util.ByteString
import play.api.libs.iteratee._

/**
  * @see http://greweb.me/2012/08/zound-a-playframework-2-audio-streaming-experiment-using-iteratees/
  */
class StreamManager(val rawStream: Enumerator[ByteString],
                    val channel: Concurrent.Channel[ByteString]) {
  @volatile
  var isReceivingStream: Boolean = false
  // Not sure how helpful this is but it does not seem to break things
  // The intention is to reduce the number of HTTP chunks sent per second
  private val chunker = Enumeratee.grouped(
    Traversable.take[ByteString](5000) transform Iteratee.consume[ByteString]()
  )
  val chunkedStream = rawStream through chunker
  // Not sure how sharedChunkedStream is better than chunkedStream but
  // using it does not seem to break things
  //  val (sharedChunkedStream, _) = Concurrent.broadcast(chunkedStream)

  def eofAndEnd(): Unit = channel.eofAndEnd()
}

object StreamManager {
  def empty() = {
    val (rawStream, channel) = Concurrent.broadcast[ByteString]
    fromStream(rawStream, channel)
  }

  def fromStream(rawStream: Enumerator[ByteString], channel: Concurrent.Channel[ByteString]) =
    new StreamManager(rawStream, channel)
}
