/*
 * Copyright (C) 2018-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.actor.typed;

import org.apache.pekko.Done;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.junit.Test;
import org.scalatestplus.junit.JUnitSuite;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertFalse;

public class ActorSystemTest extends JUnitSuite {

  @Test
  public void testGetWhenTerminated() throws Exception {
    final ActorSystem<Void> system =
        ActorSystem.create(Behaviors.empty(), "GetWhenTerminatedSystem");
    system.terminate();
    final CompletionStage<Done> cs = system.getWhenTerminated();
    cs.toCompletableFuture().get(2, SECONDS);
  }

  @Test
  public void testGetWhenTerminatedWithoutTermination() {
    final ActorSystem<Void> system =
        ActorSystem.create(Behaviors.empty(), "GetWhenTerminatedWithoutTermination");
    assertFalse(system.getWhenTerminated().toCompletableFuture().isDone());
  }
}
