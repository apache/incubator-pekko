/*
 * Copyright (C) 2019-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.testkit

import scala.util.control.NoStackTrace

/**
 * A predefined exception that can be used in tests. It doesn't include a stack trace.
 */
final case class TestException(message: String) extends RuntimeException(message) with NoStackTrace
