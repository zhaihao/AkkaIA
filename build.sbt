version          := "0.1.0-SNAPSHOT"
scalaVersion     := "2.13.8"
name             := "akkaia"
organization     := "me.ooon"
idePackagePrefix := Some("me.ooon.akkaia")

libraryDependencies ++= Seq(NSCALA, OS_LIB, SQUANTS, ORISON, TYPESAFE_CONFIG, PLAY_JSON)
libraryDependencies ++= Seq(SCALA_TEST, LOG, AKKA).flatten

excludeDependencies ++= Seq(
  ExclusionRule("org.slf4j", "slf4j-log4j12"),
  ExclusionRule("log4j", "log4j")
)