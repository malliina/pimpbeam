package com.mle.beam

import com.mle.play.PlaySecurity
import com.mle.play.auth.BasicCredentials
import com.mle.play.controllers.AuthResult
import controllers.Home
import play.api.mvc.RequestHeader

/**
 * @author Michael
 */
object PhoneSecurity extends PlaySecurity {
  override def authenticate(implicit request: RequestHeader): Option[AuthResult] = {
    authenticateFromHeader.map(u => AuthResult(u))
  }

  /**
   * Validates the supplied credentials, which are valid if:
   *
   * 1) a player is connected with the same non-empty username as the one supplied here
   * 2) the default password is correct
   *
   * @return true if the credentials are valid, false otherwise
   */
  override def validateCredentials(creds: BasicCredentials): Boolean = {
    val user = creds.username
    log debug s"Validating: $user"
    Home.validateCredentials(creds) && ensurePlayerExists(user)
  }

  private def ensurePlayerExists(username: String): Boolean = {
    val playerExists = (Players client username).nonEmpty
    if (!playerExists) {
      log warn s"Unauthorized connection attempt as user: $username. No such player is connected."
    }
    playerExists
  }
}
