/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2020-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package docs.org.apache.pekko.persistence.typed

import org.scalatest.wordspec.AnyWordSpecLike
import org.apache.pekko
import pekko.actor.testkit.typed.scaladsl.LogCapturing
import pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import pekko.actor.typed.ActorRef
import pekko.actor.typed.Behavior
import pekko.persistence.testkit.PersistenceTestKitPlugin
import pekko.persistence.testkit.query.scaladsl.PersistenceTestKitReadJournal
import pekko.persistence.typed.ReplicaId
import pekko.persistence.typed.ReplicationId
import pekko.persistence.typed.crdt.ORSet
import pekko.persistence.typed.scaladsl.Effect
import pekko.persistence.typed.scaladsl.EventSourcedBehavior
import pekko.persistence.typed.scaladsl.ReplicatedEventSourcing

object ReplicatedMovieWatchListExampleSpec {
  // #movie-entity
  object MovieWatchList {
    sealed trait Command
    final case class AddMovie(movieId: String) extends Command
    final case class RemoveMovie(movieId: String) extends Command
    final case class GetMovieList(replyTo: ActorRef[MovieList]) extends Command
    final case class MovieList(movieIds: Set[String])

    def apply(entityId: String, replicaId: ReplicaId, allReplicaIds: Set[ReplicaId]): Behavior[Command] = {
      ReplicatedEventSourcing.commonJournalConfig(
        ReplicationId("movies", entityId, replicaId),
        allReplicaIds,
        PersistenceTestKitReadJournal.Identifier) { replicationContext =>
        EventSourcedBehavior[Command, ORSet.DeltaOp, ORSet[String]](
          replicationContext.persistenceId,
          ORSet.empty(replicationContext.replicaId),
          (state, cmd) => commandHandler(state, cmd),
          (state, event) => eventHandler(state, event))
      }
    }

    private def commandHandler(state: ORSet[String], cmd: Command): Effect[ORSet.DeltaOp, ORSet[String]] = {
      cmd match {
        case AddMovie(movieId) =>
          Effect.persist(state + movieId)
        case RemoveMovie(movieId) =>
          Effect.persist(state - movieId)
        case GetMovieList(replyTo) =>
          replyTo ! MovieList(state.elements)
          Effect.none
      }
    }

    private def eventHandler(state: ORSet[String], event: ORSet.DeltaOp): ORSet[String] = {
      state.applyOperation(event)
    }

  }
  // #movie-entity

}

class ReplicatedMovieWatchListExampleSpec
    extends ScalaTestWithActorTestKit(PersistenceTestKitPlugin.config)
    with AnyWordSpecLike
    with LogCapturing {
  import ReplicatedMovieWatchListExampleSpec._

  "MovieWatchList" must {
    "demonstrate ORSet" in {
      import MovieWatchList._

      val Replicas = Set(ReplicaId("DC-A"), ReplicaId("DC-B"))

      val dcAReplica: ActorRef[Command] = spawn(MovieWatchList("mylist", ReplicaId("DC-A"), Replicas))
      val dcBReplica: ActorRef[Command] = spawn(MovieWatchList("mylist", ReplicaId("DC-B"), Replicas))

      val probeA = createTestProbe[MovieList]()
      val probeB = createTestProbe[MovieList]()

      dcAReplica ! AddMovie("movie-15")
      dcAReplica ! AddMovie("movie-17")
      dcBReplica ! AddMovie("movie-20")

      eventually {
        dcAReplica ! GetMovieList(probeA.ref)
        probeA.expectMessage(MovieList(Set("movie-15", "movie-17", "movie-20")))
        dcBReplica ! GetMovieList(probeB.ref)
        probeB.expectMessage(MovieList(Set("movie-15", "movie-17", "movie-20")))
      }

      dcBReplica ! RemoveMovie("movie-17")
      eventually {
        dcAReplica ! GetMovieList(probeA.ref)
        probeA.expectMessage(MovieList(Set("movie-15", "movie-20")))
        dcBReplica ! GetMovieList(probeB.ref)
        probeB.expectMessage(MovieList(Set("movie-15", "movie-20")))
      }
    }

  }

}
