import com.malliina.sbt.unix.LinuxKeys.{httpPort, httpsPort}
import com.malliina.sbt.unix.LinuxPlugin
import com.malliina.sbtplay.PlayProject
import com.typesafe.sbt.SbtNativePackager.{Debian, Linux, Universal}
import com.typesafe.sbt.packager
import com.typesafe.sbt.packager.Keys.serverLoading
import com.typesafe.sbt.packager.archetypes.{JavaServerAppPackaging, ServerLoader}
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoPlugin}

object BeamBuild {
  lazy val pimpbeam = PlayProject.default("pimpbeam")
    .enablePlugins(JavaServerAppPackaging, BuildInfoPlugin)
    .settings(beamSettings: _*)

  lazy val beamSettings = Seq(
    version := "1.9.0",
    scalaVersion := "2.11.8",
    libraryDependencies ++= deps,
    retrieveManaged := false,
    fork in Test := true,
    resolvers += Resolver.bintrayRepo("malliina", "maven")
  ) ++ nativeSettings ++ buildMetaSettings

  def buildMetaSettings = Seq(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion),
    buildInfoPackage := "com.malliina.beam"
  )

  def nativeSettings = LinuxPlugin.playSettings ++ Seq(
    httpPort in Linux := Option("disabled"),
    httpsPort in Linux := Option("8457"),
    packager.Keys.maintainer := "Michael Skogberg <malliina123@gmail.com>",
    javaOptions in Universal += "-Dlogger.resource=prod-logger.xml",
    serverLoading in Debian := ServerLoader.Systemd
  )

  val malliinaGroup = "com.malliina"

  lazy val deps = Seq(
    malliinaGroup %% "util-play" % "3.6.4",
    "net.glxn" % "qrgen" % "1.3"
  )
}
