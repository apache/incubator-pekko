/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.compat

import scala.collection.immutable
import scala.concurrent.{ ExecutionContext, Future => SFuture }

import scala.annotation.nowarn

import org.apache.pekko
import pekko.annotation.InternalApi
import pekko.util.ccompat._

/**
 * INTERNAL API
 *
 * Compatibility wrapper for `scala.concurrent.Future` to be able to compile the same code
 * against Scala 2.12, 2.13
 *
 * Remove these classes as soon as support for Scala 2.12 is dropped!
 */
@nowarn @InternalApi private[pekko] object Future {
  def fold[T, R](futures: IterableOnce[SFuture[T]])(zero: R)(op: (R, T) => R)(
      implicit executor: ExecutionContext): SFuture[R] =
    SFuture.fold[T, R](futures)(zero)(op)(executor)

  def fold[T, R](futures: immutable.Iterable[SFuture[T]])(zero: R)(op: (R, T) => R)(
      implicit executor: ExecutionContext): SFuture[R] =
    SFuture.foldLeft[T, R](futures)(zero)(op)(executor)

  def reduce[T, R >: T](futures: IterableOnce[SFuture[T]])(op: (R, T) => R)(
      implicit executor: ExecutionContext): SFuture[R] =
    SFuture.reduce[T, R](futures)(op)(executor)

  def reduce[T, R >: T](futures: immutable.Iterable[SFuture[T]])(op: (R, T) => R)(
      implicit executor: ExecutionContext): SFuture[R] =
    SFuture.reduceLeft[T, R](futures)(op)(executor)

  def find[T](futures: IterableOnce[SFuture[T]])(p: T => Boolean)(
      implicit executor: ExecutionContext): SFuture[Option[T]] =
    SFuture.find[T](futures)(p)(executor)

  def find[T](futures: immutable.Iterable[SFuture[T]])(p: T => Boolean)(
      implicit executor: ExecutionContext): SFuture[Option[T]] =
    SFuture.find[T](futures)(p)(executor)
}
