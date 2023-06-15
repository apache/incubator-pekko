/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

package org.apache.pekko.io.dns

import org.apache.pekko.annotation.InternalApi

import java.security.SecureRandom
import java.util.concurrent.ThreadLocalRandom

/**
 * INTERNAL API
 *
 * These are called by an actor, however they are called inside composed futures so need to be
 * nextId needs to be thread safe.
 */
@InternalApi
private[pekko] trait IdGenerator {
  def nextId(): Short
}

/**
 * INTERNAL API
 */
@InternalApi
private[pekko] object IdGenerator {
  private val MaxUnsignedShort = 65535

  sealed trait Policy

  object Policy {
    case object ThreadLocalRandom extends Policy
    case object SecureRandom extends Policy
    val Default: Policy = ThreadLocalRandom

    def apply(name: String): Option[Policy] = name.toLowerCase match {
      case "thread-local-random" => Some(ThreadLocalRandom)
      case "secure-random"       => Some(SecureRandom)
      case _                     => Some(ThreadLocalRandom)
    }
  }

  def apply(policy: Policy): IdGenerator = policy match {
    case Policy.ThreadLocalRandom => random(ThreadLocalRandom.current())
    case Policy.SecureRandom      => random(new SecureRandom())
  }

  def apply(): IdGenerator = random(ThreadLocalRandom.current())

  /**
   * @return a random sequence of ids for production
   */
  def random(rand: java.util.Random): IdGenerator =
    () => (rand.nextInt(MaxUnsignedShort) - Short.MinValue).toShort
}
