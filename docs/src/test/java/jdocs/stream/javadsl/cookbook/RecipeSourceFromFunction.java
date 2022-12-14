/*
 * Copyright (C) 2015-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package jdocs.stream.javadsl.cookbook;

import org.apache.pekko.NotUsed;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.stream.javadsl.Sink;
import org.apache.pekko.stream.javadsl.Source;
import org.apache.pekko.testkit.javadsl.TestKit;
import com.typesafe.config.ConfigFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RecipeSourceFromFunction extends RecipeTest {
  static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system =
        ActorSystem.create(
            "RecipeSourceFromFunction",
            ConfigFactory.parseString(
                "pekko.loglevel=DEBUG\nakka.loggers = [org.apache.pekko.testkit.TestEventListener]"));
  }

  @AfterClass
  public static void tearDown() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void beMappingOfRepeat() throws Exception {
    new TestKit(system) {
      final String builderFunction() {
        return UUID.randomUUID().toString();
      }

      {
        // #source-from-function
        final Source<String, NotUsed> source =
            Source.repeat(NotUsed.getInstance()).map(elem -> builderFunction());
        // #source-from-function

        final List<String> result =
            source
                .take(2)
                .runWith(Sink.seq(), system)
                .toCompletableFuture()
                .get(3, TimeUnit.SECONDS);

        Assert.assertEquals(2, result.size());
      }
    };
  }
}
