/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.stream

final class NeverMaterializedException(cause: Throwable)
    extends RuntimeException("Downstream canceled without triggering lazy source materialization", cause) {

  def this() = this(null)

}
