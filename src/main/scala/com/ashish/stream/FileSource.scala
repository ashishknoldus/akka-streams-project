package com.ashish.stream

import java.io.{BufferedInputStream, FileInputStream}
import java.nio.file.Paths

import akka.stream.{IOResult, OverflowStrategy}
import akka.stream.scaladsl.{FileIO, Framing, Sink, Source, StreamConverters}
import akka.util.ByteString

import scala.concurrent.Future

sealed trait FileSource

object ReadFileSource extends FileSource {
  def apply(path: String, delimiter: String, maximumFrameLength: Int = 4096): Source[String, Future[IOResult]] = {
    val inStream =  new BufferedInputStream(new FileInputStream(path))
    StreamConverters.fromInputStream(() => inStream)
      .via(Framing.delimiter(ByteString(delimiter), maximumFrameLength, true))
      .map(_.utf8String)
      .buffer(maximumFrameLength, OverflowStrategy.backpressure)
  }
}

object WriteBytesFileSource extends FileSource {
  def apply(path: String): Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get(path))
}
