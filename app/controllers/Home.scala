package controllers

import java.util.UUID

import akka.stream.Materializer
import com.malliina.beam._
import com.malliina.play.AppConf
import com.malliina.play.BeamStrings._
import com.malliina.play.auth.BasicCredentials
import com.malliina.play.controllers.{BaseSecurity, Caching}
import com.malliina.play.http.HttpConstants
import com.malliina.play.http.HttpConstants.AudioMpeg
import com.malliina.play.json.JsonMessages
import com.malliina.play.models.{Password, Username}
import com.malliina.play.streams.StreamParsers
import com.malliina.storage.StorageInt
import controllers.Home.{isHttpAvailable, isHttpsAvailable, log}
import net.glxn.qrgen.QRCode
import play.api.Logger
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.iteratee._
import play.api.libs.json.JsObject
import play.api.libs.json.Json._
import play.api.libs.ws.WSRequest
import play.api.mvc._

import scala.concurrent.Future

/**
  * @see http://greweb.me/2012/08/zound-a-playframework-2-audio-streaming-experiment-using-iteratees/
  */
object Home {
  private val log = Logger(getClass)

  // reads settings
  lazy val isHttpAvailable = AppConf.isHttpAvailable
  lazy val isHttpsAvailable = AppConf.isHttpsAvailable

  def validateCredentials(creds: BasicCredentials): Boolean = {
    val user = creds.username
    // the password is not really a secret
    val credsOk = user.name.nonEmpty && creds.password == Password("beam")
    if (!credsOk) {
      log warn s"Invalid credentials provided as user '$user'."
    }
    credsOk
  }
}

class Home(beamConf: BeamConf,
           players: Players,
           phones: BeamClientStore[BeamClient],
           disco: DiscoGs,
           mat: Materializer)
  extends BaseSecurity(mat)
    with Controller {
  val maxSize = 1024.megs

  def ping = Action(Caching.NoCacheOk(BeamMessages.version))

  // Player actions

  /**
    * The landing page of the browser, after which image is called, after which stream is called by the browser if a
    * mobile device has happened to connect and started streaming something.
    */
  def index = Action { request =>
    val user = UUID.randomUUID().toString
    val remoteIP = request.remoteAddress
    log info s"Created user '$user' from '$remoteIP'."
    Ok(BeamTags.index).withSession(
      Security.username -> user
    )
  }

  /**
    * Not sure if the websocket connection has been opened by the time the call to this resource is made, hence the
    * "Limited" security check, but it matters little.
    */
  def image = PlayerLimitedSecureAction { user =>
    Action {
      val qrText = stringify(coordinate(user))
      log info s"Generating image with QR code: $qrText"
      val qrFile = QRCode.from(qrText).withSize(768, 768).file()
      val enumerator = Enumerator.fromFile(qrFile)
      Caching.NoCache(Ok.chunked(enumerator))
    }
  }

  /** Loads an album cover from DiscoGs into memory, then sends it to the client.
    *
    * Set the route that calls this action as the `src` attribute of an `img` element to display the cover.
    *
    * @return an action that returns the cover of `artist`s `album`
    */
  def cover(artist: String, album: String) = PlayerSecureAction { player =>
    log info s"Searching for cover: $artist - $album"
    Action.async {
      streamedRequest(disco.request(artist, album), _ => defaultCover)
    }
  }

  /** Performs a web request as configured by `requestHolder` and returns a stream of the response body as a [[Result]].
    *
    * However, if the request returns with a non-200 status code, `onError` is run and returned.
    *
    * @param requestHolder request to execute
    * @param onError       error handler
    * @return the result
    * @see https://www.playframework.com/documentation/2.3.x/ScalaWS
    */
  def streamedRequest(requestHolder: WSRequest, onError: Int => Result = Status): Future[Result] = {
    requestHolder.getStream().map(pair => {
      val (response, body) = pair

      def header(name: String) = response.headers.get(name).flatMap(_.headOption)

      val statusCode = response.status
      if (statusCode == play.api.http.Status.OK) {
        val contentType = header(HeaderNames.CONTENT_TYPE) getOrElse MimeTypes.BINARY
        header(HeaderNames.CONTENT_LENGTH).map(length => {
          Ok.feed(body).as(contentType).withHeaders(HeaderNames.CONTENT_LENGTH -> length)
        }).getOrElse {
          Ok.chunked(body).as(contentType)
        }
      } else {
        onError(statusCode)
      }
    })
  }

  def defaultCover = Redirect(routes.Assets.at("img/guitar.png"))

  private def coordinate(user: Username): JsObject = obj(
    BEAM_HOST -> beamConf.host,
    PORT -> beamConf.port,
    SSL_PORT -> beamConf.sslPort,
    USER -> user,
    SUPPORTS_PLAINTEXT -> isHttpAvailable,
    SUPPORTS_TLS -> isHttpsAvailable
  )

  /** Opens a connection to the client by returning a chunked HTTP response. This connection streams music to the client.
    *
    * The browser sets the audio source to "stream" only after being directed by the server; therefore we can require it
    * to be authenticated by this time, as the reception of a message already implies it is connected and authenticated.
    */
  def stream = PlayerSecureAction { player =>
    Action { req =>
      log info s"Sending stream to player '${player.user}'..."
      Ok.chunked(player.stream).as(AudioMpeg).withHeaders(
        CACHE_CONTROL -> HttpConstants.NoCache
      )
    }
  }

  // Phone actions

  def playerState = PhoneLimitedSecureAction { user =>
    Action { req =>
      val (exists, ready) = players.connections.client(user).map(player => {
        val isReady = !player.streamer.isReceivingStream
        (true, isReady)
      }).getOrElse((false, false))
      val json = BeamMessages.playerExists(user, exists, ready)
      Caching.NoCacheOk(json)
    }
  }

  /**
    * Replaces the playlist with the uploaded file.
    */
  def resetAndReceiveFile = PhoneSecureAction { player =>
    player.resetStream()
    tryPushFile(player)
  }

  /**
    * Adds the uploaded file to the playlist.
    */
  def receiveFile = PhoneSecureAction(tryPushFile)

  def clientFor(user: Username) = phones.client(user)

  private def tryPushFile(player: PlayerClient): EssentialAction = {
    val user = player.user
    val streamer = player.streamer
    if (streamer.isReceivingStream) {
      // Prevents two songs from playing at the same time in the player
      // A consequence of this implementation is that adding a track to
      // the playlist while a previous track is still being uploaded
      // will fail
      // TODO play with enumerators etc and see if this can be fixed
      streamRefusalResponse(user)
    } else {
      serveFile(user, streamer)
    }
  }

  private def serveFile(user: Username, streamer: StreamManager): EssentialAction = {
    log info s"Serving file to user '$user'..."
    streamer.isReceivingStream = true
    // the body parser pushes the request content to streamer.channel
    Action(StreamParsers.multiPartByteStreaming(bytes => Future.successful(streamer.channel.push(bytes)), maxSize)(mat)) { request =>
      log info s"Served file to '$user'."
      streamer.eofAndEnd()
      val remoteIP = request.remoteAddress
      request.body.files.foreach { file =>
        log info s"Pushed file '${file.filename}' of size ${file.ref} bytes from '$remoteIP' to user '$user'."
      }
      streamer.isReceivingStream = false
      Ok
    }
  }

  private def streamRefusalResponse(user: Username): EssentialAction =
    Action { request =>
      log warn s"Refused upload from '${request.remoteAddress}' to user '$user' because another upload to that user is currently in progress."
      BadRequest(JsonMessages.failure("Concurrent streaming to the same player is not supported; try again later."))
    }

  /**
    * Authenticated action for phones.
    *
    * Checks that credentials are set in the header and that a corresponding player is connected with the same userid.
    */
  private def PhoneSecureAction(f: PlayerClient => EssentialAction): EssentialAction =
    authenticatedAsync(authenticatePhone, onUnauthorized)(f)

  /**
    * Checks that credentials in the header exist, but does not check whether a corresponding player is connected. So
    * this validation is not sufficient to allow file uploads.
    */
  def PhoneLimitedSecureAction(f: Username => EssentialAction) =
    authenticatedAsync(headerUsername, onUnauthorized)(f)

  /** Authenticated action for connected players.
    *
    * Checks the session username and requires a previously open player connection.
    */
  def PlayerSecureAction(f: PlayerClient => EssentialAction) =
    authenticatedAsync(players.auth, onUnauthorized)(f)

  /**
    * Checks that a username is set in the session.
    */
  def PlayerLimitedSecureAction(f: Username => EssentialAction) =
    authenticatedAsync(players.authUser, onUnauthorized)(a => f(a.user))

  private def authenticatePhone(request: RequestHeader): Future[Option[PlayerClient]] =
    headerUsername(request) map { maybeUser =>
      maybeUser flatMap { user => players.connections.client(user) }
    }

  private def headerUsername(request: RequestHeader): Future[Option[Username]] =
    authenticateFromHeader(request)

  override def validateCredentials(creds: BasicCredentials): Future[Boolean] =
    Future.successful(Home.validateCredentials(creds))
}
