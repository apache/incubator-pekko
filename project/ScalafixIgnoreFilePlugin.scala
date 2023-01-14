/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2018-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko

import sbt.plugins.JvmPlugin
import sbt.{ AutoPlugin, PluginTrigger, Plugins, ScalafixSupport }
import scalafix.sbt.ScalafixPlugin

object ScalafixIgnoreFilePlugin extends AutoPlugin with ScalafixSupport {
  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = JvmPlugin && ScalafixPlugin
  import sbt._
  lazy val scalafixIgnoredSetting: Seq[Setting[_]] = if (ScalafixSupport.fixTestScope) Nil else Seq(ignore(Test))

  override def projectSettings: Seq[Def.Setting[_]] =
    scalafixIgnoredSetting ++ Seq(
      addProjectCommandsIfAbsent(alias = "fixall", value = ";scalafixEnable;scalafixAll;test:compile;reload"))
}
