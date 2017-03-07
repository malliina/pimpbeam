package com.malliina.beam

import akka.stream.Materializer
import com.malliina.play.auth.BasicCredentials
import com.malliina.play.controllers.BaseSecurity
import com.malliina.play.http.AuthedRequest
import controllers.Home
import play.api.mvc.RequestHeader

import scala.concurrent.Future

class PlayerSecurity(mat: Materializer)
  extends BaseSecurity(mat) {

  override def authenticate(rh: RequestHeader): Future[Option[AuthedRequest]] =
    authenticateFromSession(rh) map { maybe =>
      maybe.map(user => AuthedRequest(user, rh))
    }

  override def validateCredentials(creds: BasicCredentials): Future[Boolean] =
    Future.successful(Home.validateCredentials(creds))
}
