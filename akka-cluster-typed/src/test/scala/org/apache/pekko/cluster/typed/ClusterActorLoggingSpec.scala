/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.cluster.typed

import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import org.apache.pekko
import pekko.actor.ExtendedActorSystem
import pekko.actor.testkit.typed.scaladsl.LogCapturing
import pekko.actor.testkit.typed.scaladsl.LoggingTestKit
import pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import pekko.actor.typed.internal.ActorMdc
import pekko.actor.typed.scaladsl.Behaviors

object ClusterActorLoggingSpec {
  def config = ConfigFactory.parseString("""
    akka.actor.provider = cluster
    akka.remote.classic.netty.tcp.port = 0
    akka.remote.artery.canonical.port = 0
    akka.remote.artery.canonical.hostname = 127.0.0.1
    """)
}

class ClusterActorLoggingSpec
    extends ScalaTestWithActorTestKit(ClusterActorLoggingSpec.config)
    with AnyWordSpecLike
    with Matchers
    with LogCapturing {

  "Logging from an actor in a cluster" must {

    "include host and port in sourceActorSystem mdc entry" in {

      def addressString = system.classicSystem.asInstanceOf[ExtendedActorSystem].provider.addressString

      val behavior =
        Behaviors.setup[String] { context =>
          context.log.info("Starting")
          Behaviors.empty
        }

      LoggingTestKit
        .info("Starting")
        .withCustom { event =>
          event.mdc(ActorMdc.AkkaAddressKey) == addressString
        }
        .expect {
          spawn(behavior)
        }
    }
  }
}
