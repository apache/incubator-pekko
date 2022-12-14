/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package jdocs.actor;

// #my-bounded-classic-actor
import org.apache.pekko.dispatch.BoundedMessageQueueSemantics;
import org.apache.pekko.dispatch.RequiresMessageQueue;

public class MyBoundedActor extends MyActor
    implements RequiresMessageQueue<BoundedMessageQueueSemantics> {}
// #my-bounded-classic-actor
