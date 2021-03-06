name := "akka-actor-tutorial"

version := "0.1"

scalaVersion := "2.12.13"

ThisBuild / useCoursier := false

val akkaVersion = "2.6.14"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.8" % Test
)
