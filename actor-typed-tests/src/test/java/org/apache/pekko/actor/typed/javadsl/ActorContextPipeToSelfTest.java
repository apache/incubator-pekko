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

package org.apache.pekko.actor.typed.javadsl;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

import com.typesafe.config.ConfigFactory;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturing;
import org.apache.pekko.actor.testkit.typed.javadsl.LoggingTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.Props;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.scalatestplus.junit.JUnitSuite;

public final class ActorContextPipeToSelfTest extends JUnitSuite {

  @ClassRule
  public static final TestKitJunitResource testKit =
      new TestKitJunitResource(
          ConfigFactory.parseString(
              "pipe-to-self-spec-dispatcher.executor = thread-pool-executor\n"
                  + "pipe-to-self-spec-dispatcher.type = PinnedDispatcher\n"));

  @Rule public final LogCapturing logCapturing = new LogCapturing();

  static final class Msg {
    final String response;
    final String selfName;
    final String threadName;

    Msg(final String response, final String selfName, final String threadName) {
      this.response = response;
      this.selfName = selfName;
      this.threadName = threadName;
    }
  }

  @Test
  public void handlesSuccess() {
    assertEquals("ok: hi", responseFrom(CompletableFuture.completedFuture("hi"), "success"));
  }

  @Test
  public void handlesFailure() {
    assertEquals("ko: boom", responseFrom(failedFuture(new RuntimeException("boom")), "failure"));
  }

  @Test
  public void handleAdaptedNull() {
    final TestProbe<String> probe = testKit.createTestProbe();
    ActorRef<String> actor =
        testKit.spawn(
            Behaviors.setup(
                context -> {
                  CompletableFuture<String> future = new CompletableFuture<>();
                  context.pipeToSelf(
                      future,
                      (ok, ko) -> {
                        // should happen even if ok is null
                        probe.ref().tell("adapting");
                        if (ko == null) // but we pass it on if there is no exception rather than
                          // non-null ok val
                          return ok;
                        // is not allowed
                        else throw new RuntimeException(ko);
                      });

                  return Behaviors.receive(String.class)
                      .onMessageEquals(
                          "complete-with-null",
                          () -> {
                            future.complete(null);
                            return Behaviors.same();
                          })
                      .onAnyMessage(
                          msg -> {
                            probe.ref().tell(msg);
                            return Behaviors.same();
                          })
                      .build();
                }));

    LoggingTestKit.warn(
            "Adapter function returned null which is not valid as an actor message, ignoring")
        .expect(
            testKit.system(),
            () -> {
              actor.tell("complete-with-null");
              probe.expectMessage("adapting");
              probe.expectNoMessage(Duration.ofMillis(200));
              return null;
            });
  }

  private CompletableFuture<String> failedFuture(final Throwable ex) {
    final CompletableFuture<String> future = new CompletableFuture<>();
    future.completeExceptionally(ex);
    return future;
  }

  private String responseFrom(final CompletionStage<String> future, String postfix) {
    final TestProbe<Msg> probe = testKit.createTestProbe();
    final Behavior<Msg> behavior =
        Behaviors.setup(
            context -> {
              context.pipeToSelf(
                  future,
                  (string, exception) -> {
                    final String response;
                    if (string != null) response = String.format("ok: %s", string);
                    else if (exception != null)
                      response = String.format("ko: %s", exception.getMessage());
                    else response = "???";
                    return new Msg(
                        response,
                        context.getSelf().path().name(),
                        Thread.currentThread().getName());
                  });
              return Behaviors.receiveMessage(
                  msg -> {
                    probe.getRef().tell(msg);
                    return Behaviors.stopped();
                  });
            });
    final String name = "pipe-to-self-spec-" + postfix;
    final Props props = Props.empty().withDispatcherFromConfig("pipe-to-self-spec-dispatcher");

    testKit.spawn(behavior, name, props);

    final Msg msg = probe.expectMessageClass(Msg.class);

    assertEquals(name, msg.selfName);
    assertThat(
        msg.threadName, startsWith("ActorContextPipeToSelfTest-pipe-to-self-spec-dispatcher"));
    return msg.response;
  }
}
