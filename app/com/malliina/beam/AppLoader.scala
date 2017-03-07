package com.malliina.beam

import com.malliina.beam.AppComponents.log
import com.malliina.play.app.DefaultApp
import controllers.{Assets, Home}
import play.api.ApplicationLoader.Context
import play.api.routing.Router
import play.api.{BuiltInComponentsFromContext, Logger}
import router.Routes

import scala.concurrent.Future

class AppLoader extends DefaultApp(ctx => new AppComponents(ctx))

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) {
  implicit val ec = materializer.executionContext
  // Services
  val disco = new DiscoGs(materializer)
  val playerSec = new PlayerSecurity(materializer)
  val phoneClients = new BeamClientStore[BeamClient]()
  val playerClients = new BeamClientStore[PlayerClient]()
  val players = new Players(playerSec, playerClients, phoneClients, materializer)
  val phoneSec = new PhoneSecurity(players, materializer)
  val phones = new Phones(phoneSec, phoneClients, playerClients, materializer)
  val conf = new ConfReader(configuration).load
  // Controllers
  lazy val assets = new Assets(httpErrorHandler)
  val home = new Home(conf, players, phoneClients, disco, materializer)
  override val router: Router = new Routes(httpErrorHandler, home, phones, players, assets)

  log info s"Started MusicBeamer endpoint. Advertising address: ${conf.host}:${conf.port}/${conf.sslPort}."

  applicationLifecycle.addStopHook(() => Future.successful {
    disco.close()
  })
}

object AppComponents {
  private val log = Logger(getClass)
}
