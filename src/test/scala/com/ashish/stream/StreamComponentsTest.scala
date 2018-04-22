package com.ashish.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{GraphDSL, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy, SourceShape}
import akka.testkit.TestKit
import com.ashish.stream.FilterStringsFlow.Regex
import org.scalatest.{AsyncWordSpecLike, MustMatchers}

import scala.concurrent.ExecutionContext

class StreamComponentsTest extends TestKit(ActorSystem("test-actor-system")) with AsyncWordSpecLike with MustMatchers {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(system)
  implicit val ec: ExecutionContext = system.dispatcher

  val testPipeOnePath = "test-files/pipe-one"
  val testPipeTwoPath = "test-files/pipe-two"

  private val classLoader = getClass.getClassLoader
  
  private val pipe1Path = classLoader.getResource(testPipeOnePath).getFile
  private val pipe2Path = classLoader.getResource(testPipeTwoPath).getFile

  private val outputList = List[(String, String)](("R1_1", "R2_9"), ("B1_4", "B2_6"), ("B1_8", "B2_8"),
    ("G1_5", "G2_10"), ("R1_2", "R2_20"))

  private val compositeSource = Source.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>

      import GraphDSL.Implicits._

      val pipe1 = ReadBufferedFileSource(pipe1Path, " ", overflowStrategy = OverflowStrategy.backpressure)
      val pipe2 = ReadBufferedFileSource(pipe2Path, " ", overflowStrategy = OverflowStrategy.backpressure)

      val filterPipeOneRElements = FilterStringsFlow(Regex("[R].{3,}"))
      val filterPipeOneGElements = FilterStringsFlow(Regex("[G].{3,}"))
      val filterPipeOneBElements = FilterStringsFlow(Regex("[B].{3,}"))
      val filterPipeTwoRElements = FilterStringsFlow(Regex("[R].{3,}"))
      val filterPipeTwoGElements = FilterStringsFlow(Regex("[G].{3,}"))
      val filterPipeTwoBElements = FilterStringsFlow(Regex("[B].{3,}"))

      val broadcastPipeOne = FlowBroadcast[String](3)
      val broadcastPipeTwo = FlowBroadcast[String](3)

      val broadcastOutput = FlowBroadcast[(String, String)](2)

      val zipR = StringsToTupleZipper()
      val zipG = StringsToTupleZipper()
      val zipB = StringsToTupleZipper()

      val tupleMerger = TupleMerger[(String, String)](3)

      val consoleOutput = Sink.foreach[(String, String)](print)

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

      broadcastOutput ~> consoleOutput
      SourceShape(broadcastOutput.out(1)) //For testing purpose
  })

  "Composite source should give correct output after processing both pipes" in {
    compositeSource.runWith(Sink.head).map { tuple2 =>
      assert(outputList.contains(tuple2))
    }
  }

}
