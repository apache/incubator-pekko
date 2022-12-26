/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.stream.impl

import org.apache.pekko
import pekko.annotation.InternalApi
import pekko.stream.{ Attributes, Outlet, SourceShape }
import pekko.stream.impl.Stages.DefaultAttributes
import pekko.stream.stage.{ GraphStage, GraphStageLogic, OutHandler }

/**
 * INTERNAL API
 */
@InternalApi private[pekko] final class FailedSource[T](failure: Throwable) extends GraphStage[SourceShape[T]] {
  val out = Outlet[T]("FailedSource.out")
  override val shape = SourceShape(out)

  override protected def initialAttributes: Attributes = DefaultAttributes.failedSource

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with OutHandler {

      override def onPull(): Unit = ()

      override def preStart(): Unit = {
        failStage(failure)
      }
      setHandler(out, this)
    }

  override def toString = s"FailedSource(${failure.getClass.getName})"
}
