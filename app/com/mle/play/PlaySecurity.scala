package com.mle.play

import play.api.mvc.{Results, SimpleResult, Security, RequestHeader}
import org.apache.commons.codec.binary.Base64
import play.api.http.HeaderNames
import com.mle.util.Log
import Results._
import play.api.mvc.Security.AuthenticatedBuilder
import com.mle.play.controllers.BaseSecurity

/**
 *
 * @author mle
 */
trait PlaySecurity extends BaseSecurity with Log {

  class AuthAction[U](f: RequestHeader => Option[U])
    extends AuthenticatedBuilder[U](f, req => onUnauthorized(req))

//  class BasicAuthAction(f: (String, String) => Boolean)
//    extends AuthAction(request => headerAuth(f)(request))

  class SessionAuthAction extends AuthAction(req => authenticateFromSession(req))
}

