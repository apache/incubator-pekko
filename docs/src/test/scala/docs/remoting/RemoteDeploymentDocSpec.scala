/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package docs.remoting

import org.apache.pekko.actor.{ Actor, ActorRef, ActorSystem, ExtendedActorSystem }
import org.apache.pekko.testkit.{ ImplicitSender, PekkoSpec }
//#import
import org.apache.pekko
import pekko.actor.{ Address, AddressFromURIString, Deploy, Props }
import pekko.remote.RemoteScope
//#import

object RemoteDeploymentDocSpec {

  class SampleActor extends Actor {
    def receive = { case _ => sender() ! self }
  }

}

class RemoteDeploymentDocSpec extends PekkoSpec("""
    pekko.actor.provider = remote
    pekko.remote.classic.netty.tcp.port = 0
    pekko.remote.artery.canonical.port = 0
    pekko.remote.use-unsafe-remote-features-outside-cluster = on
""") with ImplicitSender {

  import RemoteDeploymentDocSpec._

  val other = ActorSystem("remote", system.settings.config)
  val address =
    other.asInstanceOf[ExtendedActorSystem].provider.getExternalAddressFor(Address("akka", "s", "host", 1)).get

  override def afterTermination(): Unit = { shutdown(other) }

  "demonstrate programmatic deployment" in {
    // #deploy
    val ref = system.actorOf(Props[SampleActor]().withDeploy(Deploy(scope = RemoteScope(address))))
    // #deploy
    ref.path.address should be(address)
    ref ! "test"
    expectMsgType[ActorRef].path.address should be(address)
  }

  def makeAddress(): Unit = {
    // #make-address-artery
    val one = AddressFromURIString("akka://sys@host:1234")
    val two = Address("akka", "sys", "host", 1234) // this gives the same
    // #make-address-artery
  }

  "demonstrate address extractor" in {
    // #make-address
    val one = AddressFromURIString("akka://sys@host:1234")
    val two = Address("akka", "sys", "host", 1234) // this gives the same
    // #make-address
    one should be(two)
  }

  "demonstrate sampleActor" in {
    // #sample-actor

    val actor = system.actorOf(Props[SampleActor](), "sampleActor")
    actor ! "Pretty slick"
    // #sample-actor
  }

}
