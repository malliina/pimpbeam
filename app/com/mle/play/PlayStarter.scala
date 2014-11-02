package com.mle.play

import java.nio.file.Files

import _root_.controllers.Home
import com.mle.file.FileUtilities
import com.mle.util.Log

/**
 * Starts Play Framework 2, does not create a RUNNING_PID file.
 *
 * An alternative to the official ways to start Play,
 * this integrates better with my more generic init scripts.
 *
 * @author mle
 */
object PlayStarter extends PlayLifeCycle with Log {
  val appName: String = com.mle.beam.BuildInfo.name

  var serverUtils: Option[ServerUtils] = None

  override def start(): Unit = {
    super.start()
    startServer()
  }

  def startServer() {
    nettyServer.foreach(server => {
      val utils = new ServerUtils(server)
      serverUtils = Some(utils)
      Home.isHttpAvailable = utils.isHttpAvailable
      Home.isHttpsAvailable = utils.isHttpsAvailable
      //      log info s"${Home.isHttpAvailable} ${Home.isHttpsAvailable}"
    })
    sysProp(keyStoreKey).foreach(keyStorePath => {
      val path = FileUtilities.pathTo(keyStorePath).toAbsolutePath
      val exists = Files.exists(path)
      log info s"Path: $path exists: $exists"
    })
  }

  private def sysProp(key: String) = sys.props.get(key)
}