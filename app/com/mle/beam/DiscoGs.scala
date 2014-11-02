package com.mle.beam

import com.mle.http.WebUtils
import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.ws.WS
import play.api.libs.ws.ning.NingWSClient


/**
 *
 * @author mle
 */
class DiscoGs {
  implicit val client = new NingWSClient(new AsyncHttpClientConfig.Builder().build())

  def coverUrl(artist: String, album: String) = {
    val artistEnc = WebUtils.encodeURIComponent(artist)
    val albumEnc = WebUtils.encodeURIComponent(album)
    s"https://api.musicpimp.org/covers?artist=$artistEnc&album=$albumEnc"
  }

  def requestHolder(artist: String, album: String) = WS.clientUrl(coverUrl(artist, album))

  def close(): Unit = client.close()
}

object DiscoGs extends DiscoGs