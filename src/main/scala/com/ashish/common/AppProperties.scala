package com.ashish.common

object AppProperties {

  import Utilities.conf

  val PIPE1_PATH = conf.getString("pipe.path1")
  val PIPE2_PATH = conf.getString("pipe.path2")
  val OUTPUT_PATH = conf.getString("output.path")

}
