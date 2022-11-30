/*
 * Copyright (C) 2019-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package docs.stream.operators.sourceorflow

import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorRef
import org.apache.pekko.stream.WatchedActorTerminatedException
import org.apache.pekko.stream.scaladsl.Flow

object Watch {

  def someActor(): ActorRef = ???

  def watchExample(): Unit = {
    // #watch
    val ref: ActorRef = someActor()
    val flow: Flow[String, String, NotUsed] =
      Flow[String].watch(ref).recover {
        case _: WatchedActorTerminatedException => s"$ref terminated"
      }
    // #watch
  }

}
