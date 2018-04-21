scalaVersion := "2.12.5"
resolvers ++= Seq(
  ivyRepo("bintray-sbt-plugin-releases",
    "http://dl.bintray.com/content/sbt/sbt-plugin-releases"),
  ivyRepo("malliina bintray sbt",
    "https://dl.bintray.com/malliina/sbt-plugins/"),
  Resolver.bintrayRepo("malliina", "maven")
)

scalacOptions ++= Seq("-unchecked", "-deprecation")

addSbtPlugin("com.malliina" %% "sbt-play" % "1.2.2")

dependencyOverrides ++= Seq(
  "org.scala-js" % "sbt-scalajs" % "0.6.22",
  "org.webjars" % "webjars-locator-core" % "0.33",
  "org.codehaus.plexus" % "plexus-utils" % "3.0.17",
  "com.google.guava" % "guava" % "23.0"
)

def ivyRepo(name: String, urlString: String) =
  Resolver.url(name, url(urlString))(Resolver.ivyStylePatterns)
