package com.ashish.stream

import akka.NotUsed
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import akka.util.ByteString
import com.ashish.common.AppProperties._
import com.ashish.stream.AsyncBufferedFlow.Regex

object RGBGraph {

  def apply(): RunnableGraph[NotUsed] = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>

      import GraphDSL.Implicits._

      val pipe1 = ReadFileSource(PIPE1_PATH, " ")
      val pipe2 = ReadFileSource(PIPE2_PATH, " ")

      val channelOneElementsFlow = Flow[String].map(List(_))
      val channelTwoElementsFlow = Flow[String].map(List(_))

      val channelOneRElements = AsyncBufferedFlow(Regex("[R].{3,}"))
      val channelOneGElements = AsyncBufferedFlow(Regex("[G].{3,}"))
      val channelOneBElements = AsyncBufferedFlow(Regex("[B].{3,}"))
      val channelTwoRElements = AsyncBufferedFlow(Regex("[R].{3,}"))
      val channelTwoGElements = AsyncBufferedFlow(Regex("[G].{3,}"))
      val channelTwoBElements = AsyncBufferedFlow(Regex("[B].{3,}"))

      val broadcastElementsChannelOne = FlowBroadcast[List[String]](3)
      val broadcastElementsChannelTwo = FlowBroadcast[List[String]](3)

      val broadcastOutput = FlowBroadcast[ByteString](2)

      val zipR = StringsToTupleZipper()
      val zipG = StringsToTupleZipper()
      val zipB = StringsToTupleZipper()

      val tupleMerger = TupleMerger[ByteString]()

      val consoleOutput = Sink.foreach[ByteString](byteString => print(byteString.utf8String + " "))
      val fileOutput = WriteBytesFileSource(OUTPUT_PATH)

      pipe1 ~> channelOneElementsFlow ~> broadcastElementsChannelOne ~> channelOneRElements ~> zipR.in0
      broadcastElementsChannelOne ~> channelOneGElements ~> zipG.in0
      broadcastElementsChannelOne ~> channelOneBElements ~> zipB.in0

      pipe2 ~> channelTwoElementsFlow ~> broadcastElementsChannelTwo ~> channelTwoRElements ~> zipR.in1
      broadcastElementsChannelTwo ~> channelTwoGElements ~> zipG.in1
      broadcastElementsChannelTwo ~> channelTwoBElements ~> zipB.in1

      zipR.out ~> tupleMerger
      zipG.out ~> tupleMerger
      zipB.out ~> tupleMerger

      tupleMerger ~> broadcastOutput

      broadcastOutput ~> fileOutput
      broadcastOutput ~> consoleOutput

      ClosedShape
  })
}
