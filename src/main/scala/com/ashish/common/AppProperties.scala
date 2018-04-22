package com.ashish.common

import akka.stream.OverflowStrategy

object AppProperties {

  import Utilities.conf

  val PIPE1_PATH = conf.getString("pipe.path1")
  val PIPE2_PATH = conf.getString("pipe.path2")
  val OUTPUT_PATH = conf.getString("output.path")

  val OF_STRATEGY = conf.getString("overflow.strategy") match {
    case "dropHead" => OverflowStrategy.dropHead
    case "dropTail" => OverflowStrategy.dropTail
    case "dropBuffer" => OverflowStrategy.dropBuffer
    case "dropHead" => OverflowStrategy.dropHead
    case "fail" => OverflowStrategy.fail
    case _ => OverflowStrategy.backpressure
  }

}
