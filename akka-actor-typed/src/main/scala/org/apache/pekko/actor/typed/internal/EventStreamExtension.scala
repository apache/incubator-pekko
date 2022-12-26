/*
 * Copyright (C) 2019-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.actor.typed.internal

import org.apache.pekko
import pekko.actor.typed._
import pekko.actor.typed.eventstream.EventStream
import pekko.actor.typed.internal.adapter.EventStreamAdapter
import pekko.actor.typed.scaladsl.adapter._
import pekko.annotation.InternalApi

/**
 * INTERNAL API
 *
 * Exposes a typed actor that interacts with the [[pekko.actor.ActorSystem.eventStream]].
 *
 * It is used as an extension to ensure a single instance per actor system.
 */
@InternalApi private[pekko] final class EventStreamExtension(actorSystem: ActorSystem[_]) extends Extension {
  val ref: ActorRef[EventStream.Command] =
    actorSystem.internalSystemActorOf(EventStreamAdapter.behavior, "eventstream", Props.empty)
}

private[pekko] object EventStreamExtension extends ExtensionId[EventStreamExtension] {
  override def createExtension(system: ActorSystem[_]): EventStreamExtension = new EventStreamExtension(system)
}
