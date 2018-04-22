package com.ashish.stream

import akka.NotUsed
import akka.stream.ClosedShape
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink}
import akka.util.ByteString
import com.ashish.common.AppProperties._
import com.ashish.stream.AsyncBufferedFlow.Regex

object RGBGraph {

  def apply(): RunnableGraph[NotUsed] = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>

      import GraphDSL.Implicits._

      val pipe1 = ReadBufferedFileSource(PIPE1_PATH, " ", overflowStrategy = OF_STRATEGY)
      val pipe2 = ReadBufferedFileSource(PIPE2_PATH, " ", overflowStrategy = OF_STRATEGY)

      val filterPipeOneRElements = AsyncBufferedFlow(Regex("[R].{3,}"))
      val filterPipeOneGElements = AsyncBufferedFlow(Regex("[G].{3,}"))
      val filterPipeOneBElements = AsyncBufferedFlow(Regex("[B].{3,}"))
      val filterPipeTwoRElements = AsyncBufferedFlow(Regex("[R].{3,}"))
      val filterPipeTwoGElements = AsyncBufferedFlow(Regex("[G].{3,}"))
      val filterPipeTwoBElements = AsyncBufferedFlow(Regex("[B].{3,}"))

      val broadcastPipeOne = FlowBroadcast[String](3)
      val broadcastPipeTwo = FlowBroadcast[String](3)

      val broadcastOutput = FlowBroadcast[ByteString](2)

      val zipR = StringsToTupleZipper()
      val zipG = StringsToTupleZipper()
      val zipB = StringsToTupleZipper()

      val tupleMerger = TupleMerger[ByteString]()

      val consoleOutput = Sink.foreach[ByteString](byteString => print(byteString.utf8String + " "))
      val fileOutput = WriteBytesFileSource(OUTPUT_PATH)

      pipe1 ~> broadcastPipeOne ~> filterPipeOneRElements ~> zipR.in0
      broadcastPipeOne ~> filterPipeOneGElements ~> zipG.in0
      broadcastPipeOne ~> filterPipeOneBElements ~> zipB.in0

      pipe2 ~> broadcastPipeTwo ~> filterPipeTwoRElements ~> zipR.in1
      broadcastPipeTwo ~> filterPipeTwoGElements ~> zipG.in1
      broadcastPipeTwo ~> filterPipeTwoBElements ~> zipB.in1

      zipR.out ~> tupleMerger
      zipG.out ~> tupleMerger
      zipB.out ~> tupleMerger

      tupleMerger ~> broadcastOutput

      broadcastOutput ~> fileOutput
      broadcastOutput ~> consoleOutput

      ClosedShape
  })
}
