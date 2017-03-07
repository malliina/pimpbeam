package com.malliina.beam

import java.nio.file.Path

import com.malliina.util.BaseConfigReader
import play.api.{Configuration, Play}

class ConfReader(conf: Configuration) extends BaseConfigReader[BeamConf] {
  val hostKey = "beam.host"
  val portKey = "beam.port"
  val sslPortKey = "beam.sslPort"
  val defaultConf = BeamConf("beam.musicpimp.org", port = 80, sslPort = 443)

  override def filePath: Option[Path] = None

  override def loadOpt: Option[BeamConf] =
    fromEnvOpt orElse fromSysPropsOpt orElse fromAppConfOpt orElse Some(defaultConf)

  def fromAppConfOpt = for {
    host <- conf.getString(hostKey)
    port <- conf.getInt(portKey)
    sslPort <- conf.getInt(sslPortKey)
  } yield BeamConf(host, port, sslPort)

  def fromMapOpt(map: Map[String, String]): Option[BeamConf] =
    for {host <- map get hostKey
         port <- map get portKey
         sslPort <- map get sslPortKey
    } yield BeamConf(host, port.toInt, sslPort.toInt)
}

case class BeamConf(host: String, port: Int, sslPort: Int) {
  override def toString = s"$host:$port"
}
