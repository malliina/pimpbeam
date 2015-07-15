package com.mle.play

import com.mle.play.controllers.BaseSecurity
import com.mle.util.Log
import play.api.mvc.RequestHeader
import play.api.mvc.Security.AuthenticatedBuilder

/**
 *
 * @author mle
 */
trait PlaySecurity extends BaseSecurity with Log {

  class AuthAction[U](f: RequestHeader => Option[U])
    extends AuthenticatedBuilder[U](f, req => onUnauthorized(req))

  class SessionAuthAction extends AuthAction(req => authenticateFromSession(req))
}
