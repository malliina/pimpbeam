package tests

import java.io.FileNotFoundException
import java.nio.file.{Files, Paths}
import javax.net.ssl.SSLHandshakeException

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.malliina.http.ApacheHttpHelper
import com.malliina.util.Util
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.scalatest.FunSuite
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Await
import scala.concurrent.duration._

class HttpsTests extends FunSuite {
  val testTrustStorePassword = "???"

  val mat = ActorMaterializer()(ActorSystem("test"))
  implicit val ec = mat.executionContext

  test("can ping MusicBeamer over HTTP") {
    pingWithPlayAPI("http://beam.musicpimp.org/ping")
  }

  //  test("pinging MusicBeamer over HTTPS with the default truststore throws ConnectException") {
  //    intercept[ConnectException] {
  //      pingWithPlayAPI("https://beam.musicpimp.org/ping")
  //    }
  //  }

  test("pinging MusicBeamer over HTTPS with Apache HttpClient and default truststore throws SSLHandshakeException") {
    intercept[SSLHandshakeException] {
      pingWithApacheHttpClient()
    }
  }

  test("can ping MusicBeamer over HTTPS with Apache HttpClient and modified socket factory") {
    val fac = ApacheHttpHelper.allowAllCertificatesSocketFactory()
    val client = HttpClientBuilder.create().setSSLSocketFactory(fac).build()
    val req = new HttpGet("https://beam.musicpimp.org/ping")
    val response = client.execute(req)
    val responseContent = Option(response.getEntity) map EntityUtils.toString getOrElse "No response content"
    assert(responseContent contains "version")
  }

  test("can ping MusicBeamer over HTTPS with Apache HttpClient and modified truststore") {
    /**
      * Keystore trust.jks contains the StartSSL CA and sub-CA certificates. The keystore pw is 'murmur'. Site
      * beam.musicpimp.org uses a StartSSL certificate, so after we install the truststore, its certificate should pass
      * all validation.
      */
    installTrustStoreFile(Util.resource("trust.jks").getFile.drop(1))
    pingWithApacheHttpClient()
  }

  private def pingWithApacheHttpClient() {
    val client = HttpClientBuilder.create().build()
    val req = new HttpGet("https://beam.musicpimp.org/ping")
    val response = client.execute(req)
    val responseContent = Option(response.getEntity) map EntityUtils.toString getOrElse "No response content"
    assert(responseContent contains "version")
  }

  private def installTrustStoreFile(path: String) {
    if (!Files.exists(Paths get path)) {
      throw new FileNotFoundException(path)
    }
    System.setProperty("javax.net.ssl.trustStore", path)
    System.setProperty("javax.net.ssl.trustStorePassword", "murmur")
    System.setProperty("javax.net.ssl.trustStoreType", "JKS")
  }

  private def pingWithPlayAPI(url: String): WSResponse = {
    val client = AhcWSClient()(mat)
    val response = client.url(url).get()
    response.onComplete(_ => client.close())
    Await.result(response, 5.seconds)
  }
}
