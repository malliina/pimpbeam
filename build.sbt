import com.malliina.sbtplay.PlayProject

lazy val p = PlayProject.linux("pimpbeam")

version := "1.9.4"
scalaVersion := "2.11.8"
libraryDependencies ++= Seq(
  "com.malliina" %% "util-play" % "3.6.4",
  "net.glxn" % "qrgen" % "1.3"
)
resolvers += Resolver.bintrayRepo("malliina", "maven")
httpPort in Linux := Option("disabled")
httpsPort in Linux := Option("8457")
maintainer := "Michael Skogberg <malliina123@gmail.com>"
javaOptions in Universal += "-Dlogger.resource=prod-logger.xml"
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion)
buildInfoPackage := "com.malliina.beam"
