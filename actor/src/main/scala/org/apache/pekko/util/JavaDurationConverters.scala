/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.util
import java.time.{ Duration => JDuration }

import scala.concurrent.duration.{ Duration, FiniteDuration }

import org.apache.pekko.annotation.InternalStableApi

/**
 * INTERNAL API
 */
@InternalStableApi
private[pekko] object JavaDurationConverters {
  def asFiniteDuration(duration: JDuration): FiniteDuration = duration.asScala

  final implicit class JavaDurationOps(val self: JDuration) extends AnyVal {
    def asScala: FiniteDuration = Duration.fromNanos(self.toNanos)
  }

  final implicit class ScalaDurationOps(val self: Duration) extends AnyVal {
    def asJava: JDuration = JDuration.ofNanos(self.toNanos)
  }
}
