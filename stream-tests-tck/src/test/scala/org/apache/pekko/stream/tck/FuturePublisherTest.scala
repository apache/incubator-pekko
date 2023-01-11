/*
 * Copyright (C) 2014-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.stream.tck

import scala.concurrent.Promise

import org.reactivestreams._

import org.apache.pekko
import pekko.stream.scaladsl.Sink
import pekko.stream.scaladsl.Source

class FuturePublisherTest extends PekkoPublisherVerification[Int] {

  def createPublisher(elements: Long): Publisher[Int] = {
    val p = Promise[Int]()
    val pub = Source.future(p.future).runWith(Sink.asPublisher(false))
    p.success(0)
    pub
  }

  override def maxElementsFromPublisher(): Long = 1
}
