package com.ashish.stream

import akka.NotUsed
import akka.stream.Attributes
import akka.stream.scaladsl.Flow

sealed trait CustomFlow

object AsyncBufferedFlow extends CustomFlow {

  def apply(regex: Regex, initialBuffer: Int = 1024, maxBuffer: Int = 1048576): Flow[String, String, NotUsed] =
    Flow[String].filter(_.matches(regex.r))
      .async.withAttributes(Attributes.inputBuffer(initialBuffer, maxBuffer))

  case class Regex(r: String)
}
