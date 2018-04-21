package com.ashish.stream

import java.io.{BufferedInputStream, FileInputStream}
import java.nio.file.Paths

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Framing, Sink, Source, StreamConverters}
import akka.util.ByteString

import scala.concurrent.Future

sealed trait FileSource

object ReadFileSource extends FileSource {
  def apply(path: String,  delimiter: String, maximumFrameLenght: Int = 4096): Source[String, Future[IOResult]] = {
    val inStream =  new BufferedInputStream(new FileInputStream(path))
    StreamConverters.fromInputStream(() => inStream)
      .via(Framing.delimiter(ByteString(delimiter), maximumFrameLenght, true))
      .map(_.utf8String)
  }
}

object WriteBytesFileSource extends FileSource {
  def apply(path: String): Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get(path))
}
