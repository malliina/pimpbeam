import sbt.Keys._
import sbt._

object BuildBuild {
  // "build.sbt" goes here
  lazy val settings = Seq(
    scalaVersion := "2.10.6",
    resolvers ++= Seq(
      Resolver.url("malliina bintray sbt", url("https://dl.bintray.com/malliina/sbt-plugins"))(Resolver.ivyStylePatterns),
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Typesafe ivy releases" at "http://repo.typesafe.com/typesafe/ivy-releases/",
      "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"),
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  ) ++ sbtPlugins

  val malliinaGroup = "com.malliina"

  def sbtPlugins = Seq(
    malliinaGroup %% "sbt-packager" % "2.2.0",
    malliinaGroup %% "sbt-play" % "0.9.1",
    "com.eed3si9n" % "sbt-buildinfo" % "0.4.0"
  ) map addSbtPlugin
}
