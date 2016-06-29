name := """reactive-beacon"""

version := "0.0.2-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.11.14",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
