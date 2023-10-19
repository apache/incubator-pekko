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

package org.apache.pekko.persistence.typed.javadsl;

import org.apache.pekko.actor.testkit.typed.javadsl.LogCapturing;
import org.apache.pekko.actor.testkit.typed.javadsl.LoggingTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJUnitResource;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.persistence.typed.PersistenceId;
import org.apache.pekko.persistence.typed.RecoveryCompleted;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.scalatestplus.junit.JUnitSuite;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LoggerSourceTest extends JUnitSuite {

  private static final Config config =
      ConfigFactory.parseString(
          "pekko.persistence.journal.plugin = \"pekko.persistence.journal.inmem\" \n"
              + "pekko.persistence.journal.inmem.test-serialization = on \n");

  @ClassRule public static final TestKitJUnitResource testKit = new TestKitJUnitResource(config);

  @Rule public final LogCapturing logCapturing = new LogCapturing();

  private static final AtomicInteger idCounter = new AtomicInteger(0);

  public static PersistenceId nextId() {
    return PersistenceId.ofUniqueId("" + idCounter.incrementAndGet());
  }

  static class LoggingBehavior extends EventSourcedBehavior<String, String, String> {

    private final ActorContext<String> ctx;

    public LoggingBehavior(PersistenceId persistenceId, ActorContext<String> ctx) {
      super(persistenceId);
      this.ctx = ctx;
    }

    @Override
    public String emptyState() {
      return "";
    }

    @Override
    public CommandHandler<String, String, String> commandHandler() {
      return newCommandHandlerBuilder()
          .forAnyState()
          .onCommand(str -> str.equals("stop"), (cmd, state) -> Effect().stop())
          .onAnyCommand(
              (cmd, state) -> {
                ctx.getLog().info("command-received");
                return Effect().persist("evt");
              });
    }

    @Override
    public EventHandler<String, String> eventHandler() {
      return newEventHandlerBuilder()
          .forAnyState()
          .onAnyEvent(
              (evt, state) -> {
                ctx.getLog().info("event-received");
                return evt + state;
              });
    }

    @Override
    public SignalHandler<String> signalHandler() {
      return newSignalHandlerBuilder()
          .onSignal(
              RecoveryCompleted.instance(),
              (signal) -> {
                ctx.getLog().info("recovery-completed");
              })
          .build();
    }
  }

  public Behavior<String> behavior =
      Behaviors.setup(
          (ctx) -> {
            ctx.getLog().info("setting-up-behavior");
            return new LoggingBehavior(nextId(), ctx);
          });

  @Test
  public void verifyLogging() {
    Map<String, String> expectedMdc1 = new HashMap<>();
    expectedMdc1.put("persistenceId", "1");
    expectedMdc1.put("persistencePhase", "replay-evt");

    ActorRef<String> ref =
        LoggingTestKit.info("recovery-completed")
            .withMdc(expectedMdc1)
            .withCustom(event -> event.loggerName().equals(LoggingBehavior.class.getName()))
            .expect(
                testKit.system(),
                () -> {
                  return testKit.spawn(behavior);
                });

    // MDC persistenceId and persistencePhase for the "command-received" not included in the
    // "command-received" logging, because that is via ActorContext.log directly and
    // EventSourcedBehaviorImpl
    // isn't involved.

    LoggingTestKit.info("command-received")
        .withCustom(
            event -> {
              return event.loggerName().equals(LoggingBehavior.class.getName())
                  && event.getMdc().get("pekkoSource").equals(ref.path().toString());
            })
        .expect(
            testKit.system(),
            () -> {
              ref.tell("command");
              return null;
            });

    Map<String, String> expectedMdc3 = new HashMap<>();
    expectedMdc3.put("persistenceId", "1");
    expectedMdc3.put("persistencePhase", "running-cmd");

    LoggingTestKit.info("event-received")
        .withMdc(expectedMdc3)
        .withCustom(
            event -> {
              return event.loggerName().equals(LoggingBehavior.class.getName())
                  && event.getMdc().get("pekkoSource").equals(ref.path().toString());
            })
        .expect(
            testKit.system(),
            () -> {
              ref.tell("command");
              return null;
            });

    TestProbe<Object> probe = testKit.createTestProbe();
    ref.tell("stop");
    probe.expectTerminated(ref);
  }
}
