import sbt.Keys._
import sbt._

/**
 *
 * @author mle
 */
object BuildBuild extends Build {
  // "build.sbt" goes here
  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.10.4",
    resolvers ++= Seq(
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Typesafe ivy releases" at "http://repo.typesafe.com/typesafe/ivy-releases/",
      "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"),
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  ) ++ sbtPlugins

  val mleGroup = "com.github.malliina"

  def sbtPlugins = Seq(
    "com.typesafe.play" % "sbt-plugin" % "2.3.5",
    "com.timushev.sbt" % "sbt-updates" % "0.1.2",
    "com.eed3si9n" % "sbt-buildinfo" % "0.3.0",
    mleGroup %% "sbt-packager" % "1.2.2",
    mleGroup %% "sbt-paas-deployer" % "1.0.0",
    mleGroup %% "sbt-utils" % "0.0.5",
    mleGroup %% "sbt-play" % "0.1.1"
  ) map addSbtPlugin

  override lazy val projects = Seq(root)

  lazy val root = Project("plugins", file("."))
}

