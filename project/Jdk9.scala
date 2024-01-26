/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2017-2022 Lightbend Inc. <https://www.lightbend.com>
 */

import sbt._
import sbt.Keys._

object Jdk9 extends AutoPlugin {
  import JdkOptions.notOnJdk8

  lazy val CompileJdk9 = config("CompileJdk9").extend(Compile)

  lazy val TestJdk9 = config("TestJdk9").extend(Test).extend(CompileJdk9)

  val SCALA_SOURCE_DIRECTORY = "scala-jdk-9"
  val SCALA_TEST_SOURCE_DIRECTORY = "scala-jdk9-only"
  val JAVA_SOURCE_DIRECTORY = "java-jdk-9"
  val JAVA_TEST_SOURCE_DIRECTORY = "java-jdk9-only"

  lazy val compileJdk9Settings = Seq(
    // following the scala-2.12, scala-sbt-1.0, ... convention
    unmanagedSourceDirectories := notOnJdk8(
      Seq(
        (Compile / sourceDirectory).value / SCALA_SOURCE_DIRECTORY,
        (Compile / sourceDirectory).value / JAVA_SOURCE_DIRECTORY)),
    scalacOptions := PekkoBuild.DefaultScalacOptions.value ++ notOnJdk8(Seq("-release", "11")),
    javacOptions := PekkoBuild.DefaultJavacOptions ++ notOnJdk8(Seq("--release", "11")))

  lazy val testJdk9Settings = Seq(
    // following the scala-2.12, scala-sbt-1.0, ... convention
    unmanagedSourceDirectories := notOnJdk8(
      Seq(
        (Test / sourceDirectory).value / SCALA_TEST_SOURCE_DIRECTORY,
        (Test / sourceDirectory).value / JAVA_TEST_SOURCE_DIRECTORY)),
    scalacOptions := PekkoBuild.DefaultScalacOptions.value ++ notOnJdk8(Seq("-release", "11")),
    javacOptions := PekkoBuild.DefaultJavacOptions ++ notOnJdk8(Seq("--release", "11")),
    compile := compile.dependsOn(CompileJdk9 / compile).value,
    classpathConfiguration := TestJdk9,
    externalDependencyClasspath := (Test / externalDependencyClasspath).value)

  lazy val compileSettings = Seq(
    // It might have been more 'neat' to add the jdk9 products to the jar via packageBin/mappings, but that doesn't work with the OSGi plugin,
    // so we add them to the fullClasspath instead.
    //    Compile / packageBin / mappings
    //      ++= (CompileJdk9 / products).value.flatMap(Path.allSubpaths),
    // Since sbt-osgi upgrade to 0.9.5, the fullClasspath is no longer used on packaging when use sbt-osgi, so we have to
    // add jdk9 products to dependencyClasspathAsJars instead.
    //    Compile / fullClasspath ++= (CompileJdk9 / exportedProducts).value)
    Compile / dependencyClasspathAsJars ++= (CompileJdk9 / exportedProducts).value)

  lazy val testSettings = Seq((Test / test) := {
    (Test / test).value
    (TestJdk9 / test).value
  })

  override lazy val trigger = noTrigger
  override lazy val projectConfigurations = Seq(CompileJdk9)
  override lazy val projectSettings =
    inConfig(CompileJdk9)(Defaults.compileSettings) ++
    inConfig(CompileJdk9)(compileJdk9Settings) ++
    compileSettings ++
    inConfig(TestJdk9)(Defaults.testSettings) ++
    inConfig(TestJdk9)(testJdk9Settings) ++
    testSettings
}
