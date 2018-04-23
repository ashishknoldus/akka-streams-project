package com.ashish.stream

import akka.NotUsed
import akka.stream.scaladsl.{Flow, GraphDSL}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, BidiShape, Inlet, Outlet}
import akka.util.ByteString

import scala.collection.mutable

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

class SkewCheckerBidiFlow[A, B](bufferLimit: Int, skewMessage: String) extends GraphStage[BidiShape[A, A, B, B]] {

  val in1: Inlet[A] = Inlet[A]("in1")
  val in2: Inlet[B] = Inlet[B]("in2")
  val out1: Outlet[A] = Outlet[A]("out1")
  val out2: Outlet[B] = Outlet[B]("out2")

  private val buffer1: mutable.Queue[A] = mutable.Queue()
  private val buffer2: mutable.Queue[B] = mutable.Queue()

  override def initialAttributes = Attributes.name("SkewChecker")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    setHandler(in1, new InHandler {
      override def onPush(): Unit = {
        val element = grab(in1)

        if (buffer1.size == bufferLimit && buffer2.isEmpty) {
          buffer1.clear()
          println(skewMessage + " for stream 1") //Replace with log
        } else if (buffer1.size < bufferLimit) {
          buffer1.enqueue(element)
          if(isAvailable(out1)) {
            push(out1, buffer1.dequeue())
          }
        }
      }
    })

    setHandler(in2, new InHandler {
      override def onPush(): Unit = {
        val element = grab(in2)

        if (buffer2.size < bufferLimit) {
          buffer2.enqueue(element)

          if(isAvailable(out2)){
            push(out2, buffer2.dequeue())
          }
        } else if (buffer2.size == bufferLimit && buffer1.isEmpty) {
          buffer2.clear()
          println(skewMessage + " for stream 2") //Replace with log
        }
      }
    })

    setHandler(out1, new OutHandler {
      override def onPull(): Unit = {

        if (buffer1.nonEmpty && isAvailable(out1)) {
          push(out1, buffer1.dequeue)
        } else if (!hasBeenPulled(in1)) {
          tryPull[A](in1)
        }
      }
    })

    setHandler(out2, new OutHandler {
      override def onPull(): Unit = {

        if (buffer2.nonEmpty && isAvailable(out2)) {
          push(out2, buffer2.dequeue)
        }
        
        if (!hasBeenPulled(in2)) {
          tryPull[B](in2)
        }
      }
    })

  }

  override def shape: BidiShape[A, A, B, B] = BidiShape[A, A, B, B](in1, out1, in2, out2)

  override def toString = "SkewChecker"
}

object SkewCheckerBidiFlow {
  def apply[A, B](bufferLimit: Int, skewMessage: String = "The stream is skewed, so the buffer is being dropped")
                 (implicit builder: GraphDSL.Builder[NotUsed]): BidiShape[A, A, B, B] = {
    builder.add(new SkewCheckerBidiFlow[A, B](bufferLimit, skewMessage))
  }
}
