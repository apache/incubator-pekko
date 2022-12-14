/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.util

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.{ Benchmark, Measurement, Scope, State }

@State(Scope.Benchmark)
@Measurement(timeUnit = TimeUnit.MICROSECONDS)
class StackBench {

  class CustomSecurtyManager extends SecurityManager {
    def getTrace: Array[Class[_]] =
      getClassContext
  }

  @Benchmark
  def currentThread(): Array[StackTraceElement] = {
    Thread.currentThread().getStackTrace
  }

  @Benchmark
  def securityManager(): Array[Class[_]] = {
    (new CustomSecurtyManager).getTrace
  }

}
