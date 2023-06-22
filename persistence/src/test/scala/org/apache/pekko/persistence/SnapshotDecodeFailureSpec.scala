/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2018-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence

import org.apache.pekko
import pekko.actor.{ ActorLogging, ActorRef, Props }
import pekko.event.Logging
import pekko.testkit.{ EventFilter, ImplicitSender, TestEvent }

object SnapshotDecodeFailureSpec {
  case class Cmd(payload: String)

  class SaveSnapshotTestPersistentActor(name: String, probe: ActorRef) extends NamedPersistentActor(name) {
    def receiveCommand = {
      case Cmd(payload)            => persist(payload)(_ => saveSnapshot(payload))
      case SaveSnapshotSuccess(md) => probe ! md.sequenceNr
    }
    def receiveRecover = {
      case _ =>
    }
  }

  class LoadSnapshotTestPersistentActor(name: String, probe: ActorRef)
      extends NamedPersistentActor(name)
      with ActorLogging {

    def receiveCommand = {
      case _ =>
    }
    def receiveRecover = {
      case SnapshotOffer(_, _) => throw new Exception("kanbudong")
      case other               => probe ! other
    }
  }
}

class SnapshotDecodeFailureSpec
    extends PersistenceSpec(PersistenceSpec.config("inmem", "SnapshotDecodeFailureSpec"))
    with ImplicitSender {

  import SnapshotDecodeFailureSpec._

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    val persistentActor = system.actorOf(Props(classOf[SaveSnapshotTestPersistentActor], name, testActor))
    persistentActor ! Cmd("payload")
    expectMsg(1)
  }

  "A persistentActor with a failing snapshot loading" must {
    "fail recovery and stop actor when no snapshot could be loaded" in {
      system.eventStream.subscribe(testActor, classOf[Logging.Error])
      system.eventStream.publish(TestEvent.Mute(EventFilter[java.lang.Exception](start = "kanbudong")))
      try {
        val lPersistentActor = system.actorOf(Props(classOf[LoadSnapshotTestPersistentActor], name, testActor))
        expectMsgType[Logging.Error].message.toString should startWith(
          "Persistence failure when replaying events for persistenceId")
        watch(lPersistentActor)
        expectTerminated(lPersistentActor)
      } finally {
        system.eventStream.unsubscribe(testActor, classOf[Logging.Error])
        system.eventStream.publish(TestEvent.UnMute(EventFilter.error(start = "kanbudong")))
      }
    }

  }
}
