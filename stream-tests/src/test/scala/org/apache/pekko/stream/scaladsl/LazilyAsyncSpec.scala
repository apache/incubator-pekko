/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2014-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.stream.scaladsl

import java.util.concurrent.atomic.AtomicBoolean

import scala.annotation.nowarn
import scala.concurrent.Future

import org.apache.pekko
import pekko.Done
import pekko.stream.testkit.StreamSpec
import pekko.stream.testkit.TestSubscriber
import pekko.testkit.DefaultTimeout

import org.scalatest.concurrent.ScalaFutures

@nowarn("msg=deprecated") // tests deprecated methods
class LazilyAsyncSpec extends StreamSpec with DefaultTimeout with ScalaFutures {

  import system.dispatcher

  "A lazy async source" should {

    "work in happy path scenario" in {
      val stream = Source
        .lazilyAsync { () =>
          Future(42)
        }
        .runWith(Sink.head)

      stream.futureValue should ===(42)
    }

    "call factory method on demand only" in {
      val probe = TestSubscriber.probe[Int]()
      val constructed = new AtomicBoolean(false)

      Source
        .lazilyAsync { () =>
          constructed.set(true); Future(42)
        }
        .runWith(Sink.fromSubscriber(probe))
      probe.cancel()

      constructed.get() should ===(false)
    }

    "fail materialized value when downstream cancels without ever consuming any element" in {
      val materialization = Source
        .lazilyAsync { () =>
          Future(42)
        }
        .toMat(Sink.cancelled)(Keep.left)
        .run()

      intercept[RuntimeException] {
        materialization.futureValue
      }
    }

    "materialize when the source has been created" in {
      val probe = TestSubscriber.probe[Int]()

      val materialization: Future[Done] =
        Source
          .lazilyAsync { () =>
            Future(42)
          }
          .mapMaterializedValue(_.map(_ => Done))
          .to(Sink.fromSubscriber(probe))
          .run()

      materialization.value shouldEqual None
      probe.request(1)
      probe.expectNext(42)
      materialization.futureValue should ===(Done)

      probe.cancel()
    }

    "propagate failed future from factory" in {
      val probe = TestSubscriber.probe[Int]()
      val failure = new RuntimeException("too bad")
      Source
        .lazilyAsync { () =>
          Future.failed(failure)
        }
        .to(Sink.fromSubscriber(probe))
        .run()

      probe.request(1)
      probe.expectError(failure)
    }
  }
}
