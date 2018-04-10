import sbt.Keys.version

organization := "livesafe"
scalaVersion := "2.12.5"
name := "lagom-service-locator-consul"

val lagomVersion = "1.4.4"

val typesafeConfig = "com.typesafe" % "config" % "1.3.1"
val lagomScaladslClient = "com.lightbend.lagom" %% "lagom-scaladsl-client" % lagomVersion
val consulApi = "com.ecwid.consul" % "consul-api" % "1.3.1"
val scalatest = "org.scalatest" %% "scalatest" % "3.0.5" % Test

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion)
buildInfoPackage := "com.livesafe.lagom.discovery.consul"
buildInfoUsePackageAsPath := true

s3region := com.amazonaws.regions.Regions.US_EAST_1

/** Scala version must be part of the artifact id. */
crossPaths := true
publishArtifact in(Test, packageBin) := false // Test artifacts are not desired
publishMavenStyle := false // Ensure this publishes with Ivy conventions.
publishArtifact in(Compile, packageDoc) := false
publishArtifact in packageDoc := false
sources in(Compile, doc) := Nil

publishTo := Some(s3resolver.value("LiveSafe", s3("livesafe-artifacts/ivy2")).withIvyPatterns)

libraryDependencies ++= Seq(
  lagomScaladslClient,
  consulApi,
  scalatest
)
