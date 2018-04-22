name := "akka-streams-poc"

version := "0.1"

scalaVersion := "2.12.5"

val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.12"
val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.5.12"
val typesafeConfig = "com.typesafe" % "config" % "1.3.2"
val akkaStreamContrib = "com.typesafe.akka" %% "akka-stream-contrib" % "0.9"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test
val akkaStreamTestKit = "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.12" % Test

libraryDependencies ++= Seq(akkaStream,
  akkaActor,
  typesafeConfig,
  akkaStreamContrib,
  scalaTest,
  akkaTestKit,
  akkaStreamTestKit
)
