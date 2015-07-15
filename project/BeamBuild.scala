import com.mle.sbt.unix.LinuxKeys.{httpPort, httpsPort}
import com.mle.sbt.unix.LinuxPlugin
import com.mle.sbtplay.PlayProjects
import com.typesafe.sbt.SbtNativePackager.Linux
import com.typesafe.sbt.packager
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoPackage}

object BeamBuild extends Build {
  lazy val playGround = PlayProjects.plainPlayProject("pimpbeam").settings(beamSettings: _*)
    .enablePlugins(sbtbuildinfo.BuildInfoPlugin)

  lazy val beamSettings = Seq(
    version := "1.8.8",
    scalaVersion := "2.11.7",
    libraryDependencies ++= deps,
    retrieveManaged := false,
    fork in Test := true,
    resolvers += Resolver.bintrayRepo("malliina", "maven")
  ) ++ nativeSettings ++ buildMetaSettings

  def buildMetaSettings = Seq(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion),
    buildInfoPackage := "com.mle.beam"
  )

  def nativeSettings = LinuxPlugin.playSettings ++ Seq(
    httpPort in Linux := Option("8456"),
    httpsPort in Linux := Option("8457"),
    packager.Keys.maintainer := "Michael Skogberg <malliina123@gmail.com>"
  )

  val myGroup = "com.github.malliina"

  lazy val deps = Seq(
    "com.newrelic.agent.java" % "newrelic-agent" % "3.1.1" % "provided",
    myGroup %% "util-play" % "2.0.1",
    myGroup %% "play-base" % "0.5.1",
    "net.glxn" % "qrgen" % "1.3"
  )
}
