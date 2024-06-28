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

import org.apache.pekko.annotation.InternalApi

import java.lang.invoke.{ MethodHandles, MethodType }
import java.util.concurrent.ThreadFactory

@InternalApi
private[dispatch] object VirtualThreadSupport {

  /**
   * Returns if the current Runtime supports virtual threads.
   */
  def isSupported: Boolean = create("testIsSupported") ne null

  /**
   * Create a virtual thread factory, returns null when failed.
   */
  def create(prefix: String): ThreadFactory =
    try {
      val builderClass = ClassLoader.getSystemClassLoader.loadClass("java.lang.Thread$Builder")
      val ofVirtualClass = ClassLoader.getSystemClassLoader.loadClass("java.lang.Thread$Builder$OfVirtual")
      val lookup = MethodHandles.lookup
      val ofVirtualMethod = lookup.findStatic(classOf[Thread], "ofVirtual", MethodType.methodType(ofVirtualClass))
      var builder = ofVirtualMethod.invoke()
      val nameMethod = lookup.findVirtual(ofVirtualClass, "name",
        MethodType.methodType(ofVirtualClass, classOf[String], classOf[Long]))
      val factoryMethod = lookup.findVirtual(builderClass, "factory", MethodType.methodType(classOf[ThreadFactory]))
      builder = nameMethod.invoke(builder, prefix + "-virtual-thread-", 0L)
      factoryMethod.invoke(builder).asInstanceOf[ThreadFactory]
    } catch {
      case _: Throwable => null
    }
}
