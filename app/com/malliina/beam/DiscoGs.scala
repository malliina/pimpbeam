package com.malliina.beam

import akka.stream.Materializer
import com.malliina.http.WebUtils
import play.api.libs.ws.WSRequest
import play.api.libs.ws.ahc.AhcWSClient

class DiscoGs(mat: Materializer) {
  implicit val client = AhcWSClient()(mat)

  def request(artist: String, album: String): WSRequest =
    client.url(coverUrl(artist, album))

  def coverUrl(artist: String, album: String): String = {
    val artistEnc = WebUtils.encodeURIComponent(artist)
    val albumEnc = WebUtils.encodeURIComponent(album)
    s"https://api.musicpimp.org/covers?artist=$artistEnc&album=$albumEnc"
  }

  def close(): Unit = client.close()
}
