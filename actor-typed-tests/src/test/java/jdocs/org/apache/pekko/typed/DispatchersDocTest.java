/*
 * Copyright (C) 2018-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package jdocs.org.apache.pekko.typed;

import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.*;
import org.apache.pekko.actor.typed.DispatcherSelector;

public class DispatchersDocTest {

  public static void spawnDispatchers(ActorContext<Integer> context, Behavior<String> behavior) {
    // #spawn-dispatcher
    context.spawn(behavior, "DefaultDispatcher");
    context.spawn(behavior, "ExplicitDefaultDispatcher", DispatcherSelector.defaultDispatcher());
    context.spawn(behavior, "BlockingDispatcher", DispatcherSelector.blocking());
    context.spawn(behavior, "ParentDispatcher", DispatcherSelector.sameAsParent());
    context.spawn(
        behavior, "DispatcherFromConfig", DispatcherSelector.fromConfig("your-dispatcher"));
    // #spawn-dispatcher
  }
}
