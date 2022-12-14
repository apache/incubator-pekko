/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.osgi.test

import org.apache.pekko.actor.Actor

/**
 * Simple ping-pong actor, used for testing
 */
object PingPong {

  abstract class TestMessage

  case object Ping extends TestMessage
  case object Pong extends TestMessage

  class PongActor extends Actor {
    def receive = {
      case Ping =>
        sender() ! Pong
    }
  }

}
