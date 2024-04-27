/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pekko.dispatch

import java.util
import java.util.Collections
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

private[dispatch] class NewVirtualThreadPerTaskExecutor(threadFactory: ThreadFactory) extends AbstractExecutorService {
  import NewVirtualThreadPerTaskExecutor._

  /**
   * 0 RUNNING
   * 1 SHUTDOWN
   * 2 TERMINATED
   */
  private val state = new AtomicInteger(RUNNING)
  private val virtualThreads = ConcurrentHashMap.newKeySet[Thread]()
  private val terminateLock = new ReentrantLock()
  private val terminatedCondition = terminateLock.newCondition()

  override def shutdown(): Unit = {
    shutdown(false)
  }

  private def shutdown(interrupt: Boolean): Unit = {
    if (!isShutdown) {
      terminateLock.lock()
      try {
        if (isTerminated) {
          ()
        } else {
          if (state.compareAndSet(RUNNING, SHUTDOWN) && interrupt) {
            virtualThreads.forEach(thread => {
              if (!thread.isInterrupted) {
                thread.interrupt()
              }
            })
          }
          tryTerminateAndSignal()
        }
      } finally {
        terminateLock.unlock()
      }
    }
  }

  private def tryTerminateAndSignal(): Unit = {
    if (isTerminated) {
      ()
    }
    terminateLock.lock()
    try {
      if (isTerminated) {
        return
      }
      if (virtualThreads.isEmpty && state.compareAndSet(SHUTDOWN, TERMINATED)) {
        terminatedCondition.signalAll()
      }
    } finally {
      terminateLock.unlock()
    }
  }

  override def shutdownNow(): util.List[Runnable] = {
    shutdown(true)
    Collections.emptyList()
  }

  override def isShutdown: Boolean = state.get() >= SHUTDOWN

  override def isTerminated: Boolean = state.get() >= TERMINATED

  private def isRunning: Boolean = state.get() == RUNNING

  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = {
    if (isTerminated) {
      return true
    }
    terminateLock.lock()
    try {
      var nanosRemaining = unit.toNanos(timeout)
      while (!isTerminated && nanosRemaining > 0) {
        nanosRemaining = terminatedCondition.awaitNanos(nanosRemaining)
      }
    } finally {
      terminateLock.unlock()
    }
    isTerminated
  }

  // TODO AS only this execute method is been used in `Dispatcher.scala`, so `submit` and other methods is not override.
  override def execute(command: Runnable): Unit = {
    if (state.get() >= SHUTDOWN) {
      throw new RejectedExecutionException("Shutdown")
    }
    var started = false;
    try {
      val thread = threadFactory.newThread(Task(this, command))
      virtualThreads.add(thread)
      if (isRunning) {
        thread.start()
        started = true
      } else {
        onThreadExit(thread)
      }
    } finally {
      if (!started) {
        throw new RejectedExecutionException("Shutdown")
      }
    }
  }

  private def onThreadExit(thread: Thread): Unit = {
    virtualThreads.remove(thread)
    if (state.get() == SHUTDOWN) {
      tryTerminateAndSignal()
    }
  }
}

private[dispatch] object NewVirtualThreadPerTaskExecutor {
  private final val RUNNING = 0
  private final val SHUTDOWN = 1
  private final val TERMINATED = 2

  private case class Task(executor: NewVirtualThreadPerTaskExecutor, runnable: Runnable) extends Runnable {
    override def run(): Unit = {
      try {
        runnable.run()
      } finally {
        executor.onThreadExit(Thread.currentThread())
      }
    }
  }
}
