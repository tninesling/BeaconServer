name := """beacon"""

version := "0.0.2-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.11.14",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "ws.securesocial" %% "securesocial" % "master-SNAPSHOT"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers +=  Resolver.sonatypeRepo("snapshots")
