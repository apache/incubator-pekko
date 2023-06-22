/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2017-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.serialization

import java.nio.{ ByteBuffer, ByteOrder }

import scala.concurrent.duration._

import org.apache.pekko
import pekko.actor.ExtendedActorSystem
import pekko.testkit._

object DisabledJavaSerializerWarningSpec {
  final case class Msg(s: String)
}

class DisabledJavaSerializerWarningSpec extends PekkoSpec("""
  pekko.actor {
    allow-java-serialization = off
    serialize-messages = on
    no-serialization-verification-needed-class-prefix = []
    # this is by default on, but tests are running with off
    warn-about-java-serializer-usage = on
  }
  """) with ImplicitSender {

  import DisabledJavaSerializerWarningSpec._

  "DisabledJavaSerializer warning" must {

    "be logged for suspicious messages" in {
      EventFilter.warning(start = "Outgoing message attempted to use Java Serialization", occurrences = 1).intercept {
        val echo = system.actorOf(TestActors.echoActorProps)
        echo ! List("a")
        expectNoMessage(300.millis)
      }

    }

    "be skipped for well known local messages" in {
      EventFilter.warning(start = "Outgoing message attempted to use Java Serialization", occurrences = 0).intercept {
        val echo = system.actorOf(TestActors.echoActorProps)
        echo ! Msg("a") // Msg is in the org.apache.pekko package
        expectMsg(Msg("a"))
      }
    }

    "log and throw exception for erroneous incoming messages when Java Serialization is off" in {
      EventFilter.warning(start = "Incoming message attempted to use Java Serialization", occurrences = 1).intercept {
        intercept[DisabledJavaSerializer.JavaSerializationException] {
          val byteBuffer = ByteBuffer.allocate(128).order(ByteOrder.LITTLE_ENDIAN)
          val esys = system.asInstanceOf[ExtendedActorSystem]
          val dser = DisabledJavaSerializer(esys)
          dser.fromBinary(byteBuffer, "")
        }
      }
    }

  }
}
