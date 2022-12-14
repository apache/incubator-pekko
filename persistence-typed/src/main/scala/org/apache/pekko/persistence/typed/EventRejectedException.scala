/*
 * Copyright (C) 2018-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.typed

/**
 * Thrown if a journal rejects an event e.g. due to a serialization error.
 */
final class EventRejectedException(persistenceId: PersistenceId, sequenceNr: Long, cause: Throwable)
    extends RuntimeException(s"Rejected event, persistenceId [${persistenceId.id}], sequenceNr [$sequenceNr]", cause)
