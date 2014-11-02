package com.mle.beam

import com.mle.play.ws.SocketClient
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.JsValue

/**
 *
 * @author mle
 */
class BeamClient(val user: String, val channel: Channel[JsValue]) extends SocketClient[JsValue]