package com.malliina.play

import akka.stream.Materializer
import com.malliina.play.controllers.BaseSecurity
import play.api.mvc.RequestHeader
import play.api.mvc.Security.AuthenticatedBuilder

abstract class PlaySecurity(mat: Materializer) extends BaseSecurity(mat) {

//  class AuthAction[U](f: RequestHeader => Option[U])
//    extends AuthenticatedBuilder[U](f, req => onUnauthorized(req))
//
//  class SessionAuthAction extends AuthAction(req => authenticateFromSession(req))

}
