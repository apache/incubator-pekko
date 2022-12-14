/*
 * Copyright (C) 2016-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.remote

import org.apache.pekko.actor.Address

@SerialVersionUID(1L)
final case class UniqueAddress(address: Address, uid: Long) extends Ordered[UniqueAddress] {
  override def hashCode = java.lang.Long.hashCode(uid)

  def compare(that: UniqueAddress): Int = {
    val result = Address.addressOrdering.compare(this.address, that.address)
    if (result == 0) if (this.uid < that.uid) -1 else if (this.uid == that.uid) 0 else 1
    else result
  }

  override def toString(): String =
    address.toString + "#" + uid
}
