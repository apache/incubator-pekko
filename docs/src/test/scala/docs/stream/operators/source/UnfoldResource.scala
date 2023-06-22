/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package docs.stream.operators.source

import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.Source

object UnfoldResource {

  // imaginary blocking API we need to use
  // #unfoldResource-blocking-api
  trait Database {
    // blocking query
    def doQuery(): QueryResult
  }
  trait QueryResult {
    def hasMore: Boolean
    // potentially blocking retrieval of each element
    def nextEntry(): DatabaseEntry
    def close(): Unit
  }
  trait DatabaseEntry
  // #unfoldResource-blocking-api

  def unfoldResourceExample(): Unit = {
    implicit val actorSystem = ActorSystem()
    // #unfoldResource
    // we don't actually have one, it was just made up for the sample
    val database: Database = ???

    val queryResultSource: Source[DatabaseEntry, NotUsed] =
      Source.unfoldResource[DatabaseEntry, QueryResult](
        // open
        { () =>
          database.doQuery()
        },
        // read
        { query =>
          if (query.hasMore)
            Some(query.nextEntry())
          else
            // signals end of resource
            None
        },
        // close
        query => query.close())

    // process each element
    queryResultSource.runForeach(println)
    // #unfoldResource
  }

}
