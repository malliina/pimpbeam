import com.malliina.sbtplay.PlayProject
import play.sbt.PlayImport

lazy val p = PlayProject.linux("pimpbeam")

version := "2.1.0"
scalaVersion := "2.12.6"
libraryDependencies ++= Seq(
  "com.malliina" %% "util-play" % "4.14.0",
  "com.malliina" %% "logstreams-client" % "1.2.0",
  "net.glxn" % "qrgen" % "1.3",
  PlayImport.ws
)
dependencyOverrides ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.8",
  "com.typesafe.akka" %% "akka-actor" % "2.5.8"
)
resolvers += Resolver.bintrayRepo("malliina", "maven")
httpPort in Linux := Option("8557")
httpsPort in Linux := Option("disabled")
maintainer := "Michael Skogberg <malliina123@gmail.com>"
javaOptions in Universal ++= {
  val linuxName = (name in Linux).value
  Seq(
    s"-Dconfig.file=/etc/$linuxName/production.conf",
    s"-Dlogger.file=/etc/$linuxName/logback-prod.xml"
  )
}
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion)
buildInfoPackage := "com.malliina.beam"
