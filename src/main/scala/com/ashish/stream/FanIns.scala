package com.ashish.stream

import akka.NotUsed
import akka.stream.Attributes
import akka.stream.scaladsl.{GraphDSL, Merge, ZipWith}
import akka.util.ByteString

private[stream] sealed trait FanIns

/**
  * GraphDSL.Builder is immutable by nature!!!! Hence this object is created as package private.
  */
private[stream] object StringsToTupleZipper extends FanIns {
  def apply(initialBuffer: Int = 1, maxBuffer: Int = 1)(implicit builder: GraphDSL.Builder[NotUsed]) =
    builder.add(ZipWith[String, String, ByteString]((str1, str2) => ByteString("" + (str1, str2)))
      .async.withAttributes(Attributes.inputBuffer(initialBuffer,maxBuffer)))
}

private[stream] object TupleMerger extends FanIns {
  def apply[T]()(implicit builder: GraphDSL.Builder[NotUsed]) = builder.add(Merge[T](3))
}
