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

package docs.stream.operators.sourceorflow

//#imports
import org.apache.pekko
import pekko.actor.ActorSystem
import pekko.stream.scaladsl.Source

//#imports
object FoldWhile extends App {

  implicit val sys: ActorSystem = ActorSystem()

  // #foldWhile
  Source(1 to 10)
    .foldWhile(0)(_ < 10)(_ + _)
    .runForeach(println)
  // Expect prints:
  // 10
  // #foldWhile
}
