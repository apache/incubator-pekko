/*
 * Copyright (C) 2014-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.stream.impl

import scala.concurrent.Promise

import org.reactivestreams.{ Subscriber, Subscription }

import org.apache.pekko
import pekko.Done
import pekko.annotation.InternalApi

/**
 * INTERNAL API
 */
@InternalApi private[pekko] final class SinkholeSubscriber[T](whenComplete: Promise[Done]) extends Subscriber[T] {
  private[this] var running: Boolean = false

  override def onSubscribe(sub: Subscription): Unit = {
    ReactiveStreamsCompliance.requireNonNullSubscription(sub)
    if (running) sub.cancel()
    else {
      running = true
      sub.request(Long.MaxValue)
    }
  }

  override def onError(cause: Throwable): Unit = {
    ReactiveStreamsCompliance.requireNonNullException(cause)
    whenComplete.tryFailure(cause)
  }

  override def onComplete(): Unit = whenComplete.trySuccess(Done)

  override def onNext(element: T): Unit = ReactiveStreamsCompliance.requireNonNullElement(element)
}
