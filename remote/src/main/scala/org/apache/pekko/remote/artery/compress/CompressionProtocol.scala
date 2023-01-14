/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2016-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.remote.artery.compress

import org.apache.pekko
import pekko.actor.ActorRef
import pekko.remote.UniqueAddress
import pekko.remote.artery.ControlMessage

/** INTERNAL API */
private[remote] object CompressionProtocol {

  /** INTERNAL API */
  sealed trait CompressionMessage

  /** INTERNAL API */
  sealed trait CompressionAckMessage extends CompressionMessage

  /** INTERNAL API */
  sealed trait CompressionAdvertisement[T] extends ControlMessage with CompressionMessage {
    def from: UniqueAddress
    def table: CompressionTable[T]
  }

  /**
   * INTERNAL API
   * Sent by the "receiving" node after allocating a compression id to a given [[pekko.actor.ActorRef]]
   */
  private[remote] final case class ActorRefCompressionAdvertisement(
      from: UniqueAddress,
      table: CompressionTable[ActorRef])
      extends CompressionAdvertisement[ActorRef]

  /**
   * INTERNAL API
   * Sent by the "sending" node after receiving [[ActorRefCompressionAdvertisement]]
   * The advertisement is also confirmed by the first message using that table version,
   * but we need separate ack in case the sender is not using any of the refs in the advertised
   * table.
   */
  private[remote] final case class ActorRefCompressionAdvertisementAck(from: UniqueAddress, tableVersion: Byte)
      extends ControlMessage
      with CompressionAckMessage

  /**
   * INTERNAL API
   * Sent by the "receiving" node after allocating a compression id to a given class manifest
   */
  private[remote] final case class ClassManifestCompressionAdvertisement(
      from: UniqueAddress,
      table: CompressionTable[String])
      extends CompressionAdvertisement[String]

  /**
   * INTERNAL API
   * Sent by the "sending" node after receiving [[ClassManifestCompressionAdvertisement]]
   * The advertisement is also confirmed by the first message using that table version,
   * but we need separate ack in case the sender is not using any of the refs in the advertised
   * table.
   */
  private[remote] final case class ClassManifestCompressionAdvertisementAck(from: UniqueAddress, tableVersion: Byte)
      extends ControlMessage
      with CompressionAckMessage

  /** INTERNAL API */
  private[remote] object Events {

    /** INTERNAL API */
    private[remote] sealed trait Event

    /** INTERNAL API */
    final case class HeavyHitterDetected(key: Any, id: Int, count: Long) extends Event

    /** INTERNAL API */
    final case class ReceivedActorRefCompressionTable(from: UniqueAddress, table: CompressionTable[ActorRef])
        extends Event

    /** INTERNAL API */
    final case class ReceivedClassManifestCompressionTable(from: UniqueAddress, table: CompressionTable[String])
        extends Event

  }

}
