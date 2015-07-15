package com.mle.beam

import java.nio.file.{Paths, Path}

import play.api.{Configuration, Play}
import com.mle.util.BaseConfigReader

/**
 *
 * @author mle
 */
class ConfReader extends BaseConfigReader[BeamConf] {
  val hostKey = "beam.host"
  val portKey = "beam.port"
  val sslPortKey = "beam.sslPort"
  val defaultConf = BeamConf("beam.musicpimp.org", port = 80, sslPort = 443)

  override def userHomeConfPath: Path = Paths get "nonexistent"

  override def resourceCredential: String = "nonexistent"

  override def loadOpt: Option[BeamConf] =
    fromEnvOpt orElse fromSysPropsOpt orElse fromAppConfOpt orElse Some(defaultConf)

  def fromAppConfOpt = for {
    host <- conf(_.getString(hostKey))
    port <- conf(_.getInt(portKey))
    sslPort <- conf(_.getInt(sslPortKey))
  } yield BeamConf(host, port, sslPort)

  def fromMapOpt(map: Map[String, String]): Option[BeamConf] =
    for {host <- map get hostKey
         port <- map get portKey
         sslPort <- map get sslPortKey
    } yield BeamConf(host, port.toInt, sslPort.toInt)

  // reads from application.conf
  def maybeConfiguration = Play.maybeApplication.map(_.configuration)

  def conf[T](f: Configuration => Option[T]): Option[T] =
    maybeConfiguration flatMap f
}

object ConfReader extends ConfReader

case class BeamConf(host: String, port: Int, sslPort: Int) {
  override def toString = s"$host:$port"
}
