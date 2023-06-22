/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.cluster

import org.apache.pekko
import pekko.remote.testkit.MultiNodeConfig
import pekko.testkit._
import pekko.util.ccompat._

@ccompatUsedUntil213
object NodeMembershipMultiJvmSpec extends MultiNodeConfig {
  val first = role("first")
  val second = role("second")
  val third = role("third")

  commonConfig(debugConfig(on = false).withFallback(MultiNodeClusterSpec.clusterConfigWithFailureDetectorPuppet))
}

class NodeMembershipMultiJvmNode1 extends NodeMembershipSpec
class NodeMembershipMultiJvmNode2 extends NodeMembershipSpec
class NodeMembershipMultiJvmNode3 extends NodeMembershipSpec

abstract class NodeMembershipSpec extends MultiNodeClusterSpec(NodeMembershipMultiJvmSpec) {

  import NodeMembershipMultiJvmSpec._

  "A set of connected cluster systems" must {

    "(when two nodes) start gossiping to each other so that both nodes gets the same gossip info" taggedAs LongRunningTest in {

      // make sure that the node-to-join is started before other join
      runOn(first) {
        startClusterNode()
      }
      enterBarrier("first-started")

      runOn(first, second) {
        cluster.join(first)
        awaitAssert(clusterView.members.size should ===(2))
        assertMembers(clusterView.members, first, second)
        awaitAssert(clusterView.members.unsorted.map(_.status) should ===(Set(MemberStatus.Up)))
      }

      enterBarrier("after-1")
    }

    "(when three nodes) start gossiping to each other so that all nodes gets the same gossip info" taggedAs LongRunningTest in {

      runOn(third) {
        cluster.join(first)
      }

      awaitAssert(clusterView.members.size should ===(3))
      assertMembers(clusterView.members, first, second, third)
      awaitAssert(clusterView.members.unsorted.map(_.status) should ===(Set(MemberStatus.Up)))

      enterBarrier("after-2")
    }

    "correct member age" taggedAs LongRunningTest in {
      val firstMember = clusterView.members.find(_.address == address(first)).get
      val secondMember = clusterView.members.find(_.address == address(second)).get
      val thirdMember = clusterView.members.find(_.address == address(third)).get
      firstMember.isOlderThan(thirdMember) should ===(true)
      thirdMember.isOlderThan(firstMember) should ===(false)
      secondMember.isOlderThan(thirdMember) should ===(true)
      thirdMember.isOlderThan(secondMember) should ===(false)

      enterBarrier("after-3")

    }
  }
}
