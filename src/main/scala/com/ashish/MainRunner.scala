package com.ashish

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.ashish.stream.RGBGraph

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object MainRunner extends App {

  implicit val system = ActorSystem("actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  RGBGraph().run()

  try {
    System.in.read()
    System.exit(0)
  } catch {
    case NonFatal(ex: Exception) => system.terminate().map(_ => println(exitMessage))
  }

  private val exitMessage =
    s"\n********************************************************************\n" +
      s"********************************************************************\n" +
      s"**  Actor system is going down. Exiting the application as well.  **\n" +
      s"********************************************************************\n" +
      s"********************************************************************\n"
}
