/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko

import sbt._
import sbt.Keys._
import java.io.File
import com.lightbend.sbt.publishrsync.PublishRsyncPlugin.autoImport.publishRsyncHost
import xerial.sbt.Sonatype.autoImport.sonatypeProfileName

object Publish extends AutoPlugin {

  val defaultPublishTo = settingKey[File]("Default publish directory")

  override def trigger = allRequirements

  override lazy val projectSettings = Seq(
    publishTo := Some(akkaPublishTo.value),
    publishRsyncHost := "akkarepo@gustav.akka.io",
    credentials ++= akkaCredentials,
    organizationName := "Apache Software Foundation",
    organizationHomepage := Some(url("https://www.apache.org")),
    sonatypeProfileName := "org.apache.pekko",
    startYear := Some(2022),
    developers := List(
      Developer(
        "pekko-contributors",
        "Apache Pekko Contributors",
        "dev@pekko.apache.org",
        url("https://github.com/apache/incubator-pekko/graphs/contributors"))),
    publishMavenStyle := true,
    pomIncludeRepository := { x =>
      false
    },
    defaultPublishTo := target.value / "repository")

  private def akkaPublishTo = Def.setting {
    val key = new java.io.File(
      Option(System.getProperty("pekko.gustav.key"))
        .getOrElse(System.getProperty("user.home") + "/.ssh/id_rsa_gustav.pem"))
    if (isSnapshot.value)
      Resolver.sftp("Akka snapshots", "gustav.akka.io", "/home/akkarepo/www/snapshots").as("akkarepo", key)
    else
      Opts.resolver.sonatypeStaging
  }

  private def akkaCredentials: Seq[Credentials] =
    Option(System.getProperty("pekko.publish.credentials")).map(f => Credentials(new File(f))).toSeq
}

/**
 * For projects that are not to be published.
 */
object NoPublish extends AutoPlugin {
  override def requires = plugins.JvmPlugin

  override def projectSettings =
    Seq(publish / skip := true, Compile / doc / sources := Seq.empty)
}
