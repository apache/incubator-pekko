/*
 * Copyright (C) 2019-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.testkit.scaladsl

import java.io.NotSerializableException

import org.apache.pekko
import pekko.actor.Props
import pekko.persistence.SaveSnapshotFailure
import pekko.persistence.testkit._

class SnapshotSerializeSpec extends CommonSnapshotTests {

  override lazy val system = initSystemWithEnabledPlugin("SnapshotSerializeSpec", true, true)

  override def specificTests(): Unit =
    "fail if tries to save nonserializable snapshot" in {
      val pid = randomPid()
      val a = system.actorOf(Props(classOf[A], pid, Some(testActor)))
      a ! NewSnapshot(new C)

      expectMsg((List.empty, 0L))
      expectMsgPF() { case SaveSnapshotFailure(_, _: NotSerializableException) => }
    }

}
