package com.ashish.stream

import akka.NotUsed
import akka.stream.Attributes
import akka.stream.scaladsl.{GraphDSL, Merge, ZipWith}

private[stream] sealed trait FanIns

/**
  * GraphDSL.Builder is immutable by nature!!!! Hence this object is created as package private.
  */
private[stream] object StringsToTupleZipper extends FanIns {
  def apply(initialBuffer: Int = 256, maxBuffer: Int = 4096)(implicit builder: GraphDSL.Builder[NotUsed]) =
    builder.add(ZipWith[String, String, (String, String)](Tuple2.apply)
      .async.withAttributes(Attributes.inputBuffer(initialBuffer, maxBuffer)))
}

private[stream] object TupleMerger extends FanIns {
  def apply[T](intlets: Int)(implicit builder: GraphDSL.Builder[NotUsed]) = builder.add(Merge[T](intlets))
}
