package com.mle.beam

import com.mle.play.auth.BasicCredentials
import com.mle.play.controllers.{AuthResult, BaseSecurity}
import controllers.Home
import play.api.mvc.RequestHeader

/**
 * @author Michael
 */
object PlayerSecurity extends BaseSecurity {
  override def authenticate(implicit request: RequestHeader): Option[AuthResult] =
    authenticateFromSession(request).map(u => AuthResult(u))

  override def validateCredentials(creds: BasicCredentials): Boolean =
    Home.validateCredentials(creds)
}
