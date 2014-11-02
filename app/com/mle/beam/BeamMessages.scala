package com.mle.beam

import play.api.libs.json.Json
import com.mle.play.BeamStrings
import BeamStrings._
import Json._
import com.mle.util.Log
import play.api.libs.json.JsObject
import com.mle.play.json.JsonStrings._

/**
 * @author Michael
 */
trait BeamMessages extends Log {
  val reset = obj(CMD -> RESET)
  val version = obj(VERSION -> BuildInfo.version)

  def playerExists(user: String, exists: Boolean, ready: Boolean) =
    obj(USER -> user, EXISTS -> exists, READY -> ready)

  def partyDisconnected(user: String) =
    event(DISCONNECTED, USER -> user)

  def event(eventType: String, valuePairs: (String, JsValueWrapper)*): JsObject =
    obj(EVENT -> eventType) ++ obj(valuePairs: _*)

  def coverAvailable = event(COVER_AVAILABLE, COVER_SOURCE -> COVER_RESOURCE)
}

object BeamMessages extends BeamMessages
