name := """AnomaliesProject"""

version := "1.1.0-SNAPSHOT"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

description := "Anomalies Project"
    
scalaVersion := "2.11.8"

lazy val akkaVersion = "2.5.11"


lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "spring plugins" at "http://repo.spring.io/plugins-release/"
libraryDependencies ++= Seq(
  specs2 % Test,
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.3",
  "com.typesafe.play" %% "play-json" % "2.7.2",
  "com.yahoo.egads" % "egads" % "0.4.0",
  "com.github.andr83" %% "scalaconfig" % "0.4",
  "com.solarmosaic.client" %% "mail-client" % "0.1.0",
  "com.softwaremill.macwire" %% "macros" % "2.2.2",
  "com.softwaremill.macwire" %% "runtime" % "1.0.7",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.16.5-play26",
  "org.scalatest" %% "scalatest" % "2.2.6" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test",
  "org.scalatestplus" % "play_2.11" % "1.4.0" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
)