package com.ashish.stream

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, GraphDSL}

private[stream] sealed trait FanOuts

private[stream] object FlowBroadcast extends FanOuts {
  /**
    * GraphDSL.Builder is immutable by nature!!!! Hence this object is created as package private.
    */
  def apply[T](outlets: Int)(implicit builder: GraphDSL.Builder[NotUsed]) = builder.add(Broadcast[T](outlets))
}
