/*
 * Copyright (C) 2019-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.actor

import org.apache.pekko
import pekko.annotation.ApiMayChange
import pekko.event.LogMarker

/**
 * This is public with the purpose to document the used markers and properties of log events.
 * No guarantee that it will remain binary compatible, but the marker names and properties
 * are considered public API and will not be changed without notice.
 */
@ApiMayChange
object ActorLogMarker {

  /**
   * Marker "akkaDeadLetter" of log event for dead letter messages.
   *
   * @param messageClass The message class of the DeadLetter. Included as property "akkaMessageClass".
   */
  def deadLetter(messageClass: String): LogMarker =
    LogMarker("akkaDeadLetter", Map(LogMarker.Properties.MessageClass -> messageClass))

}
