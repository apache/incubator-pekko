/*
 * Copyright (C) 2014-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.util

/*
 * IMPORTANT: do not change this file, the line numbers are verified in LineNumberSpec!
 */

object LineNumberSpecCodeForScala {

  val oneline = (s: String) => println(s)

  val twoline = (s: String) => {
    println(s)
    Integer.parseInt(s)
  }

  val partial: PartialFunction[String, Unit] = {
    case "a" =>
    case "b" =>
  }

  def method(s: String) = () => {
    println(s)
    Integer.parseInt(s)
  }
}
