/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2018-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.io.dns.internal

import java.net.{ InetAddress, InetSocketAddress }

import scala.annotation.nowarn
import scala.collection.{ immutable => im }
import scala.concurrent.duration._
import scala.util.Try

import org.apache.pekko
import pekko.actor.{ Actor, ActorLogging, ActorRef, NoSerializationVerificationNeeded, Props, Stash }
import pekko.actor.Status.Failure
import pekko.annotation.InternalApi
import pekko.io.{ IO, Tcp, Udp }
import pekko.io.dns.{ RecordClass, RecordType, ResourceRecord }
import pekko.pattern.{ BackoffOpts, BackoffSupervisor }

/**
 * INTERNAL API
 */
@InternalApi private[pekko] object DnsClient {
  sealed trait DnsQuestion {
    def id: Short
  }
  final case class SrvQuestion(id: Short, name: String) extends DnsQuestion
  final case class Question4(id: Short, name: String) extends DnsQuestion
  final case class Question6(id: Short, name: String) extends DnsQuestion
  final case class Answer(id: Short, rrs: im.Seq[ResourceRecord], additionalRecs: im.Seq[ResourceRecord] = Nil)
      extends NoSerializationVerificationNeeded
  final case class DropRequest(id: Short)
}

/**
 * INTERNAL API
 */
@InternalApi private[pekko] class DnsClient(ns: InetSocketAddress) extends Actor with ActorLogging with Stash {

  import DnsClient._
  import context.system

  val udp: ActorRef = IO(Udp)
  val tcp: ActorRef = IO(Tcp)

  private[internal] var inflightRequests: Map[Short, (ActorRef, Message)] = Map.empty

  lazy val tcpDnsClient: ActorRef = createTcpClient()

  override def preStart(): Unit = {
    udp ! Udp.Bind(self, new InetSocketAddress(InetAddress.getByAddress(Array.ofDim(4)), 0))
  }

  def receive: Receive = {
    case Udp.Bound(local) =>
      log.debug("Bound to UDP address [{}]", local)
      context.become(ready(sender()))
      unstashAll()
    case _: Question4 =>
      stash()
    case _: Question6 =>
      stash()
    case _: SrvQuestion =>
      stash()
  }

  private def message(name: String, id: Short, recordType: RecordType): Message = {
    Message(id, MessageFlags(), im.Seq(Question(name, recordType, RecordClass.IN)))
  }

  /**
   * Silent to allow map update syntax
   */
  @nowarn()
  def ready(socket: ActorRef): Receive = {
    case DropRequest(id) =>
      log.debug("Dropping request [{}]", id)
      inflightRequests -= id
      unstashAll()

    case Question4(id, name) =>
      log.debug("Resolving [{}] (A)", name)
      sendingQueryOrStash(socket, message(name, id, RecordType.A))

    case Question6(id, name) =>
      log.debug("Resolving [{}] (AAAA)", name)
      sendingQueryOrStash(socket, message(name, id, RecordType.AAAA))

    case SrvQuestion(id, name) =>
      log.debug("Resolving [{}] (SRV)", name)
      sendingQueryOrStash(socket, message(name, id, RecordType.SRV))

    case Udp.CommandFailed(cmd) =>
      log.debug("Command failed [{}]", cmd)
      cmd match {
        case send: Udp.Send =>
          // best effort, don't throw
          Try {
            val msg = Message.parse(send.payload)
            inflightRequests.get(msg.id).foreach {
              case (s, _) =>
                s ! Failure(new RuntimeException("Send failed to nameserver"))
                inflightRequests -= msg.id
                unstashAll()
            }
          }
        case _ =>
          log.warning("Dns client failed to send {}", cmd)
      }
    case Udp.Received(data, remote) =>
      log.debug("Received message from [{}]: [{}]", remote, data)
      val msg @ Message(id, flags, questions, answerRecs, _, additionalRecs) = Message.parse(data)
      log.debug("Decoded UDP DNS response [{}]", msg)
      if (questions.isEmpty) {
        log.debug("Dns response contains no referenced question, discard the response.", id)
      } else {
        inflightRequests.get(id) match {
          case Some((replyTo, queryMsg)) =>
            if (flags.isTruncated) {
              log.debug("DNS response truncated, falling back to TCP")
              tcpDnsClient ! queryMsg
            } else {
              // check if the question is match
              val previousQuestion = msg.questions
              if (questions.head != previousQuestion.head) {
                log.warning(
                  "Dns response referenced to different question:{} expected:{} for id:{},maybe an error.",
                  questions.head, previousQuestion.head, id)
              } else {
                val response = if (flags.responseCode == ResponseCode.SUCCESS) {
                  Answer(id, answerRecs, additionalRecs)
                } else {
                  Answer(id, Nil, Nil)
                }
                replyTo ! response
                inflightRequests -= response.id
                unstashAll()
              }
            }
          case None =>
            log.debug("Client for id {} not found. Discarding response.", id)
        }
      }
    case response: Answer =>
      inflightRequests.get(response.id) match {
        case Some((replyTo, _)) =>
          replyTo ! response
          inflightRequests -= response.id
          unstashAll()
        case None =>
          log.debug("Client for id {} not found. Discarding response.", response.id)
      }
    case Udp.Unbind  => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }

  private def sendingQueryOrStash(socket: ActorRef, message: Message): Unit = {
    val id = message.id
    inflightRequests.get(id) match {
      case None =>
        inflightRequests += (id -> (sender() -> message))
        log.debug("Message [{}] to [{}]: [{}]", id, ns, message)
        socket ! Udp.Send(message.write(), ns)
      case Some((_, msg)) =>
        stash()
        log.debug("There is a in flight dns query with same id [{}], previous query: [{}].", id, msg)
    }
  }

  def createTcpClient(): ActorRef = {
    context.actorOf(
      BackoffSupervisor.props(
        BackoffOpts.onFailure(
          Props(classOf[TcpDnsClient], tcp, ns, self),
          childName = "tcpDnsClient",
          minBackoff = 10.millis,
          maxBackoff = 20.seconds,
          randomFactor = 0.1)),
      "tcpDnsClientSupervisor")
  }
}
