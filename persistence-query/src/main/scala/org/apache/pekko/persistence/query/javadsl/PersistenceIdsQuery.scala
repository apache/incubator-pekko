/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2015-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.query.javadsl

import org.apache.pekko
import pekko.NotUsed
import pekko.stream.javadsl.Source

/**
 * A plugin may optionally support this query by implementing this interface.
 */
trait PersistenceIdsQuery extends ReadJournal {

  /**
   * Query all `PersistentActor` identifiers, i.e. as defined by the
   * `persistenceId` of the `PersistentActor`.
   *
   * The stream is not completed when it reaches the end of the currently used `persistenceIds`,
   * but it continues to push new `persistenceIds` when new persistent actors are created.
   * Corresponding query that is completed when it reaches the end of the currently
   * currently used `persistenceIds` is provided by [[CurrentPersistenceIdsQuery#currentPersistenceIds]].
   */
  def persistenceIds(): Source[String, NotUsed]

}
