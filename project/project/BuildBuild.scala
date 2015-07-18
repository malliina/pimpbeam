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
      Resolver.url("malliina bintray sbt", url("https://dl.bintray.com/malliina/sbt-plugins"))(Resolver.ivyStylePatterns),
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Typesafe ivy releases" at "http://repo.typesafe.com/typesafe/ivy-releases/",
      "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"),
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  ) ++ sbtPlugins

  val mleGroup = "com.github.malliina"

  def sbtPlugins = Seq(
    "com.typesafe.play" % "sbt-plugin" % "2.4.2",
    "com.eed3si9n" % "sbt-buildinfo" % "0.4.0",
    mleGroup %% "sbt-packager" % "1.8.0",
    mleGroup %% "sbt-play" % "0.3.1"
  ) map addSbtPlugin

  override lazy val projects = Seq(root)

  lazy val root = Project("plugins", file("."))
}

