package com.malliina.beam

import akka.stream.Materializer
import com.malliina.beam.PhoneSecurity.log
import com.malliina.play.PlaySecurity
import com.malliina.play.auth.BasicCredentials
import com.malliina.play.http.AuthedRequest
import com.malliina.play.models.Username
import controllers.Home
import play.api.Logger
import play.api.mvc.RequestHeader

import scala.concurrent.Future

object PhoneSecurity {
  private val log = Logger(getClass)
}

class PhoneSecurity(players: Players, mat: Materializer) extends PlaySecurity(mat) {
  override def authenticate(rh: RequestHeader): Future[Option[AuthedRequest]] =
    authenticateFromHeader(rh).map(_.map(lift(_, rh)))

  /** Validates the supplied credentials, which are valid if:
    *
    * 1) a player is connected with the same non-empty username as the one supplied here
    * 2) the default password is correct
    *
    * @return true if the credentials are valid, false otherwise
    */
  override def validateCredentials(creds: BasicCredentials): Future[Boolean] = {
    val user = creds.username
    log debug s"Validating '$user'..."
    val isValid = Home.validateCredentials(creds) && ensurePlayerExists(user)
    Future.successful(isValid)
  }

  private def ensurePlayerExists(username: Username): Boolean = {
    val playerExists = (players.connections client username).nonEmpty
    if (!playerExists) {
      log warn s"Unauthorized connection attempt as user '$username'. No such player is connected."
    }
    playerExists
  }
}
