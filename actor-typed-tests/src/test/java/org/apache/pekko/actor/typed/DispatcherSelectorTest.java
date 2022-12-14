/*
 * Copyright (C) 2018-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.actor.typed;

public class DispatcherSelectorTest {
  // Compile time only test to verify
  // dispatcher factories are accessible from Java

  private DispatcherSelector def = DispatcherSelector.defaultDispatcher();
  private DispatcherSelector conf = DispatcherSelector.fromConfig("somepath");
  private DispatcherSelector parent = DispatcherSelector.sameAsParent();
}
