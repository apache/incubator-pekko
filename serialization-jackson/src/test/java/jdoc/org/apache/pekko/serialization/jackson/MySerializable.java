/*
 * Copyright (C) 2019-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package jdoc.org.apache.pekko.serialization.jackson;

// #marker-interface
/** Marker interface for messages, events and snapshots that are serialized with Jackson. */
public interface MySerializable {}

class MyMessage implements MySerializable {
  public final String name;
  public final int nr;

  public MyMessage(String name, int nr) {
    this.name = name;
    this.nr = nr;
  }
}
// #marker-interface
