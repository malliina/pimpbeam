package tests

import java.io.FileNotFoundException
import java.nio.file.{Files, Paths}
import javax.net.ssl.SSLHandshakeException

import com.mle.http.ApacheHttpHelper
import com.mle.util.Util
import com.ning.http.client.AsyncHttpClientConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.scalatest.FunSuite
import play.api.libs.ws.ning.NingWSClient
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 *
 * @author mle
 */
class HttpsTests extends FunSuite {
  val testTrustStorePassword = "???"
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
    implicit val client = new NingWSClient(new AsyncHttpClientConfig.Builder().build())
    val response = WS.clientUrl(url).get()
    response.onComplete(_ => client.close())
    Await.result(response, 5.seconds)
  }
}
