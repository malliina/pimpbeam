import com.mle.beam.DiscoGs
import com.mle.util.Log
import controllers.Home
import play.api.mvc.{RequestHeader, Result}
import play.api.{Application, GlobalSettings}

import scala.concurrent.Future

/**
 *
 * @author mle
 */
object Global extends GlobalSettings with Log {

  override def onStart(app: Application) {
    super.onStart(app)
    val conf = Home.beamConf
    log info s"Started MusicBeamer endpoint. Advertising address: ${conf.host}:${conf.port}/${conf.sslPort}."
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    val src = request.remoteAddress
    val path = request.path
    val exName = ex.getClass.getName
    import scala.compat.Platform.EOL
    log.warn(s"Unhandled exception for request to: $path from: $src. Exception: $exName: ${ex.getMessage}${ex.getStackTrace.mkString(EOL, EOL, EOL)}")
    // TODO add JSON error response
    super.onError(request, ex)
  }

  override def onStop(app: Application): Unit = {
    DiscoGs.close()
    super.onStop(app)
  }
}
