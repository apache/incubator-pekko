/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.actor.typed;

import java.time.Duration;

public class SchedulerTest {

  public void compileOnly() {
    // accepts a lambda
    ActorSystem<Void> system = null;
    system
        .scheduler()
        .scheduleAtFixedRate(
            Duration.ofMillis(10),
            Duration.ofMillis(10),
            () -> system.log().info("Woo!"),
            system.executionContext());
    system
        .scheduler()
        .scheduleOnce(
            Duration.ofMillis(10), () -> system.log().info("Woo!"), system.executionContext());
  }
}
