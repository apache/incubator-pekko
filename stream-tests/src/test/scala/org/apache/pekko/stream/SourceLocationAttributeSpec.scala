/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.stream

import org.apache.pekko
import pekko.stream.scaladsl.Flow
import pekko.stream.testkit.StreamSpec

class SourceLocationAttributeSpec extends StreamSpec {

  "The SourceLocation attribute" must {
    "not throw NPE" in {
      // #30138
      val f1 = Flow[Int].fold(0)(_ + _)
      val f2 = Flow[Int].fold(0)(_ + _)
      f1.join(f2).toString
    }
  }

}
