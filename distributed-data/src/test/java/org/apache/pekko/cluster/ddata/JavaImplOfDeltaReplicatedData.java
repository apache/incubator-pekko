/*
 * Copyright (C) 2017-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.cluster.ddata;

import java.util.Optional;

// same delta type
public class JavaImplOfDeltaReplicatedData
    extends AbstractDeltaReplicatedData<
        JavaImplOfDeltaReplicatedData, JavaImplOfDeltaReplicatedData>
    implements ReplicatedDelta {

  @Override
  public JavaImplOfDeltaReplicatedData mergeData(JavaImplOfDeltaReplicatedData other) {
    return this;
  }

  @Override
  public JavaImplOfDeltaReplicatedData mergeDeltaData(JavaImplOfDeltaReplicatedData other) {
    return this;
  }

  @Override
  public Optional<JavaImplOfDeltaReplicatedData> deltaData() {
    return Optional.empty();
  }

  @Override
  public JavaImplOfDeltaReplicatedData resetDelta() {
    return this;
  }

  @Override
  public JavaImplOfDeltaReplicatedData zero() {
    return new JavaImplOfDeltaReplicatedData();
  }
}
