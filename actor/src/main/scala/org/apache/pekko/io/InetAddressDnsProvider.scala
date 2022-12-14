/*
 * Copyright (C) 2018-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.io

import scala.annotation.nowarn

import org.apache.pekko.annotation.InternalApi

/**
 * INTERNAL API
 */
@nowarn("msg=deprecated")
@InternalApi
class InetAddressDnsProvider extends DnsProvider {
  override def cache: Dns = new SimpleDnsCache()
  override def actorClass = classOf[InetAddressDnsResolver]
  override def managerClass = classOf[SimpleDnsManager]
}
