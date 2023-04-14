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

import java.net.{ Inet4Address, Inet6Address, InetAddress, InetSocketAddress }
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.immutable
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }
import scala.util.control.NonFatal

import org.apache.pekko
import pekko.actor.{ Actor, ActorLogging, ActorRef, ActorRefFactory }
import pekko.annotation.InternalApi
import pekko.dispatch.ExecutionContexts
import pekko.event.LoggingAdapter
import pekko.io.SimpleDnsCache
import pekko.io.dns._
import pekko.io.dns.CachePolicy.{ Never, Ttl }
import pekko.io.dns.DnsProtocol.{ Ip, RequestType, Srv }
import pekko.io.dns.internal.DnsClient._
import pekko.pattern.{ ask, pipe, AskTimeoutException }
import pekko.util.{ Helpers, Timeout }
import pekko.util.PrettyDuration._

/**
 * INTERNAL API
 */
@InternalApi
private[io] final class AsyncDnsResolver(
    settings: DnsSettings,
    cache: SimpleDnsCache,
    clientFactory: (ActorRefFactory, List[InetSocketAddress]) => List[ActorRef])
    extends Actor
    with ActorLogging {

  import AsyncDnsResolver._

  initCache(cache)

  log.debug(
    "Using name servers [{}] and search domains [{}] with ndots={}",
    settings.NameServers,
    settings.SearchDomains,
    settings.NDots)

  private val resolvers: List[ActorRef] = clientFactory(context, settings.NameServers)

  // only supports DnsProtocol, not the deprecated Dns protocol
  // AsyncDnsManager converts between the protocols to support the deprecated protocol
  override def receive: Receive = {
    case DnsProtocol.Resolve(name, requestType) =>
      implicit val ex: ExecutionContext = ExecutionContexts.parasitic
      resolve(settings, name, requestType, resolvers, cache, log)
        .pipeTo(sender())
  }
}

/**
 * INTERNAL API
 */
@InternalApi
private[pekko] object AsyncDnsResolver {

  private val ipv4Address =
    """^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$""".r

  private val ipv6Address =
    """^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$""".r

  private[pekko] def isIpv4Address(name: String): Boolean =
    ipv4Address.findAllMatchIn(name).nonEmpty

  private[pekko] def isIpv6Address(name: String): Boolean =
    ipv6Address.findAllMatchIn(name).nonEmpty

  private def isInetAddress(name: String): Boolean =
    isIpv4Address(name) || isIpv6Address(name)

  private val Empty =
    Future.successful(Answer(-1, immutable.Seq.empty[ResourceRecord], immutable.Seq.empty[ResourceRecord]))

  case class ResolveFailedException(msg: String) extends Exception(msg)

  private val requestIdGen: AtomicInteger = new AtomicInteger(0)

  private def nextId(): Short = requestIdGen.updateAndGet { current =>
    if (current == Short.MaxValue) {
      Short.MinValue
    } else {
      current + 1
    }
  }.toShort

  private def resolve(
      settings: DnsSettings,
      name: String,
      requestType: RequestType,
      resolvers: List[ActorRef],
      cache: SimpleDnsCache,
      log: LoggingAdapter): Future[DnsProtocol.Resolved] = {
    cache.get((name, requestType)) match {
      case Some(resolved) =>
        log.debug("{} cached {}", requestType, resolved)
        Future.successful(resolved)
      case None => resolveWithResolvers(settings, name, requestType, resolvers, cache, log)
    }
  }

  private def resolveWithResolvers(
      settings: DnsSettings,
      name: String,
      requestType: RequestType,
      resolvers: List[ActorRef],
      cache: SimpleDnsCache,
      log: LoggingAdapter): Future[DnsProtocol.Resolved] = {
    // For ask to DNS Client
    implicit val ec: ExecutionContext = ExecutionContexts.parasitic
    resolveWithResolvers(settings, name, requestType, resolvers, log)
      .andThen {
        case Success(resolved) =>
          val positiveCachePolicy = settings.PositiveCachePolicy
          val negativeCachePolicy = settings.NegativeCachePolicy
          if (resolved.records.nonEmpty) {
            val minTtl = (positiveCachePolicy +: resolved.records.map(_.ttl)).min
            cache.put((name, requestType), resolved, minTtl)
          } else if (negativeCachePolicy != Never) {
            cache.put((name, requestType), resolved, negativeCachePolicy)
          }
        case Failure(_) =>
      }
  }

  private def resolveWithResolvers(
      settings: DnsSettings,
      name: String,
      requestType: RequestType,
      resolvers: List[ActorRef],
      log: LoggingAdapter): Future[DnsProtocol.Resolved] =
    if (isInetAddress(name)) {
      resolveInetAddress(name)
    } else {
      resolveWithSearch(settings, name, requestType, resolvers, log)
    }

  private def resolveWithSearch(
      settings: DnsSettings,
      name: String,
      requestType: RequestType,
      resolvers: List[ActorRef],
      log: LoggingAdapter): Future[DnsProtocol.Resolved] = resolvers match {
    case Nil =>
      Future.failed(ResolveFailedException(s"Failed to resolve $name with nameservers: ${settings.NameServers}"))
    case head :: tail =>
      implicit val timeout: Timeout = Timeout(settings.ResolveTimeout)
      implicit val ec: ExecutionContext = ExecutionContexts.parasitic
      resolveWithSearch(settings, name, requestType, head, log).recoverWith {
        case NonFatal(t) =>
          t match {
            case _: AskTimeoutException =>
              log.info("Resolve of {} timed out after {}. Trying next name server", name, timeout.duration.pretty)
            case _ =>
              log.info("Resolve of {} failed. Trying next name server {}", name, t.getMessage)
          }
          resolveWithSearch(settings, name, requestType, tail, log)
      }
  }

  private def resolveInetAddress(name: String): Future[DnsProtocol.Resolved] = {
    Future.fromTry(Try {
      val address = InetAddress.getByName(name) // only checks validity, since known to be IP address
      val record = address match {
        case _: Inet4Address           => ARecord(name, Ttl.effectivelyForever, address)
        case ipv6address: Inet6Address => AAAARecord(name, Ttl.effectivelyForever, ipv6address)
        case unexpected                => throw new IllegalArgumentException(s"Unexpected address: $unexpected")
      }
      DnsProtocol.Resolved(name, record :: Nil)
    })
  }

  private def sendQuestion(resolver: ActorRef, message: DnsQuestion)(implicit timeout: Timeout): Future[Answer] = {
    implicit val ec: ExecutionContext = ExecutionContexts.parasitic
    val result = (resolver ? message).mapTo[Answer]
    result.failed.foreach { _ =>
      resolver ! DropRequest(message.id)
    }
    result
  }

  private def resolveWithSearch(
      settings: DnsSettings,
      name: String,
      requestType: RequestType,
      resolver: ActorRef,
      log: LoggingAdapter)(implicit timeout: Timeout): Future[DnsProtocol.Resolved] = {
    if (settings.SearchDomains.nonEmpty) {
      val nameWithSearch = settings.SearchDomains.map(sd => name + "." + sd)
      // ndots is a heuristic used to try and work out whether the name passed in is a fully qualified domain name,
      // or a name relative to one of the search names. The idea is to prevent the cost of doing a lookup that is
      // obviously not going to resolve. So, if a host has less than ndots dots in it, then we don't try and resolve it,
      // instead, we go directly to the search domains, or at least that's what the man page for resolv.conf says. In
      // practice, Linux appears to implement something slightly different, if the name being searched contains less
      // than ndots dots, then it should be searched last, rather than first. This means if the heuristic wrongly
      // identifies a domain as being relative to the search domains, it will still be looked up if it doesn't resolve
      // at any of the search domains, albeit with the latency of having to have done all the searches first.
      val toResolve = if (name.count(_ == '.') >= settings.NDots) {
        name :: nameWithSearch
      } else {
        nameWithSearch :+ name
      }
      resolveFirst(toResolve, requestType, resolver, log)
    } else {
      resolve(name, requestType, resolver, log)
    }
  }

  private def resolveFirst(
      searchNames: List[String],
      requestType: RequestType,
      resolver: ActorRef,
      log: LoggingAdapter)(implicit timeout: Timeout): Future[DnsProtocol.Resolved] = {
    implicit val ec: ExecutionContext = ExecutionContexts.parasitic
    searchNames match {
      case searchName :: Nil =>
        resolve(searchName, requestType, resolver, log)
      case searchName :: remaining =>
        resolve(searchName, requestType, resolver, log).flatMap { resolved =>
          if (resolved.records.isEmpty)
            resolveFirst(remaining, requestType, resolver, log)
          else
            Future.successful(resolved)
        }
      case Nil =>
        // This can't happen
        Future.failed(new IllegalStateException("Failed to 'resolveFirst': 'searchNames' must not be empty"))
    }
  }

  private def resolve(
      name: String,
      requestType: RequestType,
      resolver: ActorRef,
      log: LoggingAdapter)(implicit timeout: Timeout): Future[DnsProtocol.Resolved] = {
    log.debug("Attempting to resolve {} with {}", name, resolver)
    implicit val ec: ExecutionContext = ExecutionContexts.parasitic
    val caseFoldedName = Helpers.toRootLowerCase(name)
    requestType match {
      case Ip(ipv4, ipv6) =>
        val ipv4Recs: Future[Answer] =
          if (ipv4)
            sendQuestion(resolver, Question4(nextId(), caseFoldedName))
          else
            Empty

        val ipv6Recs =
          if (ipv6)
            sendQuestion(resolver, Question6(nextId(), caseFoldedName))
          else
            Empty

        for {
          ipv4 <- ipv4Recs
          ipv6 <- ipv6Recs
        } yield DnsProtocol.Resolved(name, ipv4.rrs ++ ipv6.rrs, ipv4.additionalRecs ++ ipv6.additionalRecs)

      case Srv =>
        sendQuestion(resolver, SrvQuestion(nextId(), caseFoldedName))
          .map(answer => DnsProtocol.Resolved(name, answer.rrs, answer.additionalRecs))
    }
  }

  private def initCache(cache: SimpleDnsCache): Unit = {
    // avoid ever looking up localhost by pre-populating cache
    val loopback = InetAddress.getLoopbackAddress
    val (ipv4Address, ipv6Address) = loopback match {
      case ipv6: Inet6Address => (InetAddress.getByName("127.0.0.1"), ipv6)
      case ipv4: Inet4Address => (ipv4, InetAddress.getByName("::1"))
      case unknown            => throw new IllegalArgumentException(s"Loopback address was [$unknown]")
    }
    cache.put(
      "localhost" -> Ip(),
      DnsProtocol.Resolved("localhost", ARecord("localhost", Ttl.effectivelyForever, loopback) :: Nil),
      Ttl.effectivelyForever)
    cache.put(
      "localhost" -> Ip(ipv6 = false, ipv4 = true),
      DnsProtocol.Resolved("localhost", ARecord("localhost", Ttl.effectivelyForever, ipv4Address) :: Nil),
      Ttl.effectivelyForever)
    cache.put(
      "localhost" -> Ip(ipv6 = true, ipv4 = false),
      DnsProtocol.Resolved("localhost", ARecord("localhost", Ttl.effectivelyForever, ipv6Address) :: Nil),
      Ttl.effectivelyForever)
  }
}
