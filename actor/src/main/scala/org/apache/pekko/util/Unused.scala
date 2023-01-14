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

package org.apache.pekko.util

import scala.annotation.nowarn

import org.apache.pekko.annotation.InternalApi

/**
 * Marker for explicit or implicit parameter known to be unused, yet
 * still necessary from a binary compatibility perspective
 * or other reason. Useful in combination with
 * `-Ywarn-unused:explicits,implicits` compiler options.
 *
 * Extends 'deprecated' to make sure using a parameter marked @unused
 * produces a warning, and not using a parameter marked @unused does not
 * produce an 'unused parameter' warning.
 *
 * This approach is deprecated in Scala 2.13 and scheduled to be
 * removed in 2.14. Perhaps we should promote introducing an `@unused`
 * to Scala? https://contributors.scala-lang.org/t/more-error-reporting-annotations/1681/7
 *
 * INTERNAL API
 */
@nowarn("msg=deprecated")
@InternalApi private[pekko] class unused extends deprecated("unused", "")
