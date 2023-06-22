/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2019-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko

import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import sbt.Keys.sourceDirectory
import sbt.{ inConfig, Compile, Def, Plugins, Test, _ }
import spray.boilerplate.BoilerplatePlugin

object CopyrightHeaderForBoilerplate extends CopyrightHeader {
  override def requires: Plugins = BoilerplatePlugin && HeaderPlugin

  override protected def headerMappingSettings: Seq[Def.Setting[_]] = {
    super.headerMappingSettings
    Seq(Compile, Test).flatMap { config =>
      inConfig(config) {
        Seq(
          config / headerSources ++=
            (((config / sourceDirectory).value / "boilerplate") ** "*.template").get,
          headerMappings := headerMappings.value ++ Map(HeaderFileType("template") -> cStyleComment))
      }
    }
  }
}
