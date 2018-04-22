name := "akka-streams-poc"

version := "0.1"

scalaVersion := "2.12.5"

import Dependencies._

lazy val `akka-stream-project` = (project in file("."))
  .settings(libraryDependencies ++= Seq(akkaStream,
    akkaActor,
    typesafeConfig,
    akkaStreamContrib,
    scalaTest,
    akkaTestKit,
    akkaStreamTestKit))
