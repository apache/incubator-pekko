/*
 * Copyright (C) 2015-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package jdocs.ddata.protobuf;

import jdocs.ddata.TwoPhaseSet;

import org.apache.pekko.actor.ExtendedActorSystem;

public class TwoPhaseSetSerializerWithCompression extends TwoPhaseSetSerializer {
  public TwoPhaseSetSerializerWithCompression(ExtendedActorSystem system) {
    super(system);
  }

  // #compression
  @Override
  public byte[] toBinary(Object obj) {
    if (obj instanceof TwoPhaseSet) {
      return compress(twoPhaseSetToProto((TwoPhaseSet) obj));
    } else {
      throw new IllegalArgumentException("Can't serialize object of type " + obj.getClass());
    }
  }

  @Override
  public Object fromBinaryJava(byte[] bytes, Class<?> manifest) {
    return twoPhaseSetFromBinary(decompress(bytes));
  }
  // #compression
}
