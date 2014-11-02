import java.nio.file.Paths

import com.mle.sbt.GenericPlugin
import com.mle.sbt.cloud.{HerokuKeys, HerokuPlugin}
import com.mle.sbt.unix.LinuxPlugin
import com.mle.sbtplay.PlayProjects
import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.packager.linux
import sbt.Keys._
import sbt._
import sbtbuildinfo.Plugin._

object BeamBuild extends Build {
  lazy val playGround = PlayProjects.plainPlayProject("pimpbeam").settings(beamSettings: _*)

  lazy val beamSettings = Seq(
    version := "1.8.7",
    scalaVersion := "2.11.2",
    libraryDependencies ++= deps,
    retrieveManaged := false,
    fork in Test := true,
    resolvers ++= Seq(
      "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/")
  ) ++ buildMetaSettings ++ herokuSettings ++ nativeSettings

  def buildMetaSettings = buildInfoSettings ++ Seq(
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion),
    buildInfoPackage := "com.mle.beam"
  )

  def herokuSettings = HerokuPlugin.settings ++ Seq(
    HerokuKeys.heroku := Paths.get( """C:\Program Files (x86)\Heroku\bin\heroku.bat""")
  )

  def nativeSettings = SbtNativePackager.packagerSettings ++
    LinuxPlugin.debianSettings ++
    GenericPlugin.confSettings ++
    Seq(
      linux.Keys.maintainer := "Michael Skogberg <malliina123@gmail.com>"
    )

  val myGroup = "com.github.malliina"

  lazy val deps = Seq(
    "com.newrelic.agent.java" % "newrelic-agent" % "3.1.1" % "provided",
    myGroup %% "util-play" % "1.6.10",
    myGroup %% "play-base" % "0.1.2",
    "net.glxn" % "qrgen" % "1.3"
  )
}
