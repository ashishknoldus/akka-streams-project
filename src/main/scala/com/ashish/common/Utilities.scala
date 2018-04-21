package com.ashish.common

import com.typesafe.config.{Config, ConfigFactory}

object Utilities {
  val conf: Config = ConfigFactory.load()
}
