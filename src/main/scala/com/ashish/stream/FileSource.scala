package com.ashish.stream

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.stream.contrib.FileTailSource
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.stream.{IOResult, OverflowStrategy, javadsl}
import akka.util.ByteString

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

sealed trait FileSource

object ReadBufferedFileSource extends FileSource {
  def apply(path: String, delimiter: String, maximumFrameLength: Int = 4096, recordsBuffer: Int = 4096 * 6,
            overflowStrategy: OverflowStrategy = OverflowStrategy.backpressure): javadsl.Source[String, NotUsed] = {

    /**
      * FileTailSource looks for any changes done in the file after reaching the end of the file
      * */
    FileTailSource.create(Paths.get(path), maximumFrameLength, 0L, FiniteDuration(1, TimeUnit.SECONDS))
      .via(Framing.delimiter(ByteString(delimiter), maximumFrameLength, true))
      .map(_.utf8String)
      .buffer(recordsBuffer, overflowStrategy)
  }
}

object WriteBytesFileSource extends FileSource {
  def apply(path: String): Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get(path))
}
