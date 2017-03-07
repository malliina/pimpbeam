package com.malliina.beam

import akka.stream.scaladsl.SourceQueue
import com.malliina.play.models.Username
import com.malliina.play.ws.SocketClient
import play.api.libs.json.JsValue

class BeamClient(val user: Username, val channel: SourceQueue[JsValue])
  extends SocketClient[JsValue]
