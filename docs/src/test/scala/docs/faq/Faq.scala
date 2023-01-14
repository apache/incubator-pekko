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

package docs.faq

import org.apache.pekko.actor.Actor

//#exhaustiveness-check
object MyActor {
  // these are the messages we accept
  sealed abstract trait Message
  final case class FooMessage(foo: String) extends Message
  final case class BarMessage(bar: Int) extends Message

  // these are the replies we send
  sealed abstract trait Reply
  final case class BazMessage(baz: String) extends Reply
}

class MyActor extends Actor {
  import MyActor._
  def receive = {
    case message: Message =>
      message match {
        case BarMessage(bar) => sender() ! BazMessage("Got " + bar)
        // warning here:
        // "match may not be exhaustive. It would fail on the following input: FooMessage(_)"
        // #exhaustiveness-check
        case FooMessage(_) => // avoid the warning in our build logs
        // #exhaustiveness-check
      }
  }
}
//#exhaustiveness-check
