/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.stream;

import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.AkkaJUnitActorSystemResource;
import org.scalatestplus.junit.JUnitSuite;

public abstract class StreamTest extends JUnitSuite {
  protected final ActorSystem system;

  protected StreamTest(AkkaJUnitActorSystemResource actorSystemResource) {
    system = actorSystemResource.getSystem();
  }
}
