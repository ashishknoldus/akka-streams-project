package com.ashish.stream

import akka.NotUsed
import akka.stream.Attributes
import akka.stream.scaladsl.Flow
import akka.util.ByteString

sealed trait Flows

object FilterStringsFlow extends Flows {

  def apply(regex: Regex, initialBuffer: Int = 1024, maxBuffer: Int = 1048576): Flow[String, String, NotUsed] =
    Flow[String].filter(_.matches(regex.r))
      .async.withAttributes(Attributes.inputBuffer(initialBuffer, maxBuffer))

  case class Regex(r: String)
}

object TransformToByteStringFlow extends Flows {
  def apply[A](): Flow[A, ByteString, NotUsed] = Flow[A].map(x => ByteString("" + x))
}
