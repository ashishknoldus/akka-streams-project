name := "akka-streams-poc"

version := "0.1"

scalaVersion := "2.12.5"

val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.12"
val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.5.12"
val typesafeConfig = "com.typesafe" % "config" % "1.3.2"

libraryDependencies ++= Seq(akkaStream, akkaActor, typesafeConfig)
