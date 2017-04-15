import com.malliina.sbt.unix.LinuxKeys.{httpPort, httpsPort}
import com.malliina.sbt.unix.LinuxPlugin
import com.malliina.sbtplay.PlayProject
import com.typesafe.sbt.SbtNativePackager.{Linux, Universal}
import com.typesafe.sbt.packager.Keys.maintainer
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoPackage}

object BeamBuild {
  lazy val pimpbeam = PlayProject.server("pimpbeam")
    .settings(beamSettings: _*)

  lazy val beamSettings = nativeSettings ++ buildMetaSettings ++ Seq(
    version := "1.9.3",
    scalaVersion := "2.11.8",
    libraryDependencies ++= Seq(
      "com.malliina" %% "util-play" % "3.6.4",
      "net.glxn" % "qrgen" % "1.3"
    ),
    retrieveManaged := false,
    fork in Test := true,
    resolvers += Resolver.bintrayRepo("malliina", "maven")
  )

  def buildMetaSettings = Seq(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion),
    buildInfoPackage := "com.malliina.beam"
  )

  def nativeSettings = LinuxPlugin.playSettings ++ Seq(
    httpPort in Linux := Option("disabled"),
    httpsPort in Linux := Option("8457"),
    maintainer := "Michael Skogberg <malliina123@gmail.com>",
    javaOptions in Universal += "-Dlogger.resource=prod-logger.xml"
  )
}
