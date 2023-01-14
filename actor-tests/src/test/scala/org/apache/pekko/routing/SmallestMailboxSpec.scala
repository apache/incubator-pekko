/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.routing

import java.util.concurrent.ConcurrentHashMap

import scala.concurrent.Await

import org.apache.pekko
import pekko.actor.{ Actor, Props }
import pekko.testkit.{ DefaultTimeout, ImplicitSender, PekkoSpec, TestLatch }

class SmallestMailboxSpec extends PekkoSpec with DefaultTimeout with ImplicitSender {

  "smallest mailbox pool" must {

    "deliver messages to idle actor" in {
      val usedActors = new ConcurrentHashMap[Int, String]()
      val router = system.actorOf(SmallestMailboxPool(3).props(routeeProps = Props(new Actor {
        def receive = {
          case (busy: TestLatch, receivedLatch: TestLatch) =>
            usedActors.put(0, self.path.toString)
            self ! "another in busy mailbox"
            receivedLatch.countDown()
            Await.ready(busy, TestLatch.DefaultTimeout)
          case (msg: Int, receivedLatch: TestLatch) =>
            usedActors.put(msg, self.path.toString)
            receivedLatch.countDown()
          case _: String =>
        }
      })))

      val busy = TestLatch(1)
      val received0 = TestLatch(1)
      router ! ((busy, received0))
      Await.ready(received0, TestLatch.DefaultTimeout)

      val received1 = TestLatch(1)
      router ! ((1, received1))
      Await.ready(received1, TestLatch.DefaultTimeout)

      val received2 = TestLatch(1)
      router ! ((2, received2))
      Await.ready(received2, TestLatch.DefaultTimeout)

      val received3 = TestLatch(1)
      router ! ((3, received3))
      Await.ready(received3, TestLatch.DefaultTimeout)

      busy.countDown()

      val busyPath = usedActors.get(0)
      busyPath should not be null

      val path1 = usedActors.get(1)
      val path2 = usedActors.get(2)
      val path3 = usedActors.get(3)

      path1 should not be busyPath
      path2 should not be busyPath
      path3 should not be busyPath

    }
  }

}
