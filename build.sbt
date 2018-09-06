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

/** Scala version must be part of the artifact id. */
crossPaths := true
publishArtifact in(Test, packageBin) := false // Test artifacts are not desired

publishArtifact in(Compile, packageDoc) := false
publishArtifact in packageDoc := false
sources in(Compile, doc) := Nil

credentials in ThisBuild ++=
  (for (un <- sys.env.get("LIVESAFE_ARTIFACTORY_USERNAME"); pw <- sys.env.get("LIVESAFE_ARTIFACTORY_PASSWORD")) yield Credentials("Artifactory Realm", "livesafe.jfrog.io", un, pw)).toList :+
    Credentials(Path.userHome / ".livesafe" / "credentials.properties")

publishMavenStyle in ThisBuild := true
publishTo in ThisBuild := Some("LiveSafe Internal (Maven, local)" at "https://livesafe.jfrog.io/livesafe/livesafe-internal-maven-local")
resolvers in ThisBuild += "LiveSafe Engineering (Maven)" at "https://livesafe.jfrog.io/livesafe/livesafe-engineering-generic"

libraryDependencies ++= Seq(
  lagomScaladslClient,
  consulApi,
  scalatest
)
