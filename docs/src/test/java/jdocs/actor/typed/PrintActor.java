/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package jdocs.actor.typed;

import org.apache.pekko.actor.typed.*;
import org.apache.pekko.actor.typed.javadsl.*;

// #print-actor
class PrintActor extends AbstractBehavior<Integer> {

  public static Behavior<Integer> create() {
    return Behaviors.setup(PrintActor::new);
  }

  private PrintActor(ActorContext<Integer> context) {
    super(context);
  }

  @Override
  public Receive<Integer> createReceive() {
    return newReceiveBuilder()
        .onMessage(
            Integer.class,
            i -> {
              System.out.println("PrintActor: " + i);
              return Behaviors.same();
            })
        .build();
  }
}
// #print-actor
