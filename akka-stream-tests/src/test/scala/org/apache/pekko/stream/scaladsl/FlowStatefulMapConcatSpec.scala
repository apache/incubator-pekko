/*
 * Copyright (C) 2016-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.stream.scaladsl

import scala.util.control.NoStackTrace

import org.apache.pekko
import pekko.stream.ActorAttributes
import pekko.stream.Supervision
import pekko.stream.testkit._
import pekko.stream.testkit.scaladsl.TestSink

class FlowStatefulMapConcatSpec extends StreamSpec("""
    akka.stream.materializer.initial-input-buffer-size = 2
  """) with ScriptedTest {

  val ex = new Exception("TEST") with NoStackTrace

  "A StatefulMapConcat" must {

    "work in happy case" in {
      val script = Script(Seq(2) -> Seq(), Seq(1) -> Seq(1, 1), Seq(3) -> Seq(3), Seq(6) -> Seq(6, 6, 6))
      TestConfig.RandomTestRange.foreach(_ =>
        runScript(script)(_.statefulMapConcat(() => {
          var prev: Option[Int] = None
          x =>
            prev match {
              case Some(e) =>
                prev = Some(x)
                (1 to e).map(_ => x)
              case None =>
                prev = Some(x)
                List.empty[Int]
            }
        })))
    }

    "be able to restart" in {
      Source(List(2, 1, 3, 4, 1))
        .statefulMapConcat(() => {
          var prev: Option[Int] = None
          x => {
            if (x % 3 == 0) throw ex
            prev match {
              case Some(e) =>
                prev = Some(x)
                (1 to e).map(_ => x)
              case None =>
                prev = Some(x)
                List.empty[Int]
            }
          }
        })
        .withAttributes(ActorAttributes.supervisionStrategy(Supervision.restartingDecider))
        .runWith(TestSink.probe[Int])
        .request(2)
        .expectNext(1, 1)
        .request(4)
        .expectNext(1, 1, 1, 1)
        .expectComplete()
    }

    "be able to resume" in {
      Source(List(2, 1, 3, 4, 1))
        .statefulMapConcat(() => {
          var prev: Option[Int] = None
          x => {
            if (x % 3 == 0) throw ex
            prev match {
              case Some(e) =>
                prev = Some(x)
                (1 to e).map(_ => x)
              case None =>
                prev = Some(x)
                List.empty[Int]
            }
          }
        })
        .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))
        .runWith(TestSink.probe[Int])
        .request(2)
        .expectNext(1, 1)
        .requestNext(4)
        .request(4)
        .expectNext(1, 1, 1, 1)
        .expectComplete()
    }

  }

}
