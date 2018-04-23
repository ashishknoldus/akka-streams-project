package com.ashish.stream

import akka.NotUsed
import akka.stream.ClosedShape
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink}
import com.ashish.common.AppProperties._
import com.ashish.stream.FilterStringsFlow.Regex

object RGBGraph {

  def apply(): RunnableGraph[NotUsed] = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>

      import GraphDSL.Implicits._

      val pipe1 = ReadBufferedFileSource(PIPE1_PATH, " ", overflowStrategy = OF_STRATEGY)
      val pipe2 = ReadBufferedFileSource(PIPE2_PATH, " ", overflowStrategy = OF_STRATEGY)

      val filterPipeOneRElements = FilterStringsFlow(Regex("[R].{3,}"))
      val filterPipeOneGElements = FilterStringsFlow(Regex("[G].{3,}"))
      val filterPipeOneBElements = FilterStringsFlow(Regex("[B].{3,}"))
      val filterPipeTwoRElements = FilterStringsFlow(Regex("[R].{3,}"))
      val filterPipeTwoGElements = FilterStringsFlow(Regex("[G].{3,}"))
      val filterPipeTwoBElements = FilterStringsFlow(Regex("[B].{3,}"))

      val broadcastPipeOne = FlowBroadcast[String](3)
      val broadcastPipeTwo = FlowBroadcast[String](3)

      val broadcastOutput = FlowBroadcast[(String, String)](2)

      val skewCheckRStream = SkewCheckerBidiFlow[String, String](4096)
      val skewCheckGStream = SkewCheckerBidiFlow[String, String](4096)
      val skewCheckBStream = SkewCheckerBidiFlow[String, String](4096)

      val zipR = StringsToTupleZipper()
      val zipG = StringsToTupleZipper()
      val zipB = StringsToTupleZipper()

      val tupleMerger = TupleMerger[(String, String)](3)

      val tupleToByteString = TransformToByteStringFlow[(String, String)]()

      val consoleOutput = Sink.foreach[(String, String)](print)
      val fileOutput = WriteBytesFileSource(OUTPUT_PATH)

      pipe1 ~> broadcastPipeOne ~> filterPipeOneRElements ~> skewCheckRStream.in1
      broadcastPipeOne ~> filterPipeOneGElements ~> skewCheckGStream.in1
      broadcastPipeOne ~> filterPipeOneBElements ~> skewCheckBStream.in1

      pipe2 ~> broadcastPipeTwo ~> filterPipeTwoRElements ~> skewCheckRStream.in2
      broadcastPipeTwo ~> filterPipeTwoGElements ~> skewCheckGStream.in2
      broadcastPipeTwo ~> filterPipeTwoBElements ~> skewCheckBStream.in2


      skewCheckRStream.out1 ~> zipR.in0
      skewCheckRStream.out2 ~> zipR.in1

      skewCheckGStream.out1 ~> zipG.in0
      skewCheckGStream.out2 ~> zipG.in1

      skewCheckBStream.out1 ~> zipB.in0
      skewCheckBStream.out2 ~> zipB.in1

      zipR.out ~> tupleMerger
      zipG.out ~> tupleMerger
      zipB.out ~> tupleMerger

      tupleMerger ~> broadcastOutput

      broadcastOutput ~> tupleToByteString ~> fileOutput
      broadcastOutput ~> consoleOutput

      ClosedShape
  })
}
