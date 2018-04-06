import sbt.Keys.version

organization := "livesafe"

name := "lagom-service-locator-consul"

val lagomVersion = "1.4.4"

val typesafeConfig = "com.typesafe" % "config" % "1.3.1"
val lagomJavadslClient = "com.lightbend.lagom" %% "lagom-javadsl-client" % lagomVersion
val lagomScaladslClient = "com.lightbend.lagom" %% "lagom-scaladsl-client" % lagomVersion
val consulApi = "com.ecwid.consul" % "consul-api" % "1.1.10"
val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

scalaVersion := "2.12.5"
version := "1.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  lagomScaladslClient,
  consulApi,
  scalatest
)
