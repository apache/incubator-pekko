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

package org.apache.pekko.persistence.serialization

import org.apache.pekko
import pekko.actor.ActorSystem
import pekko.persistence.fsm.PersistentFSM.PersistentFSMSnapshot
import pekko.serialization.SerializationExtension
import pekko.testkit.PekkoSpec

import java.util.Base64
import scala.util.Success

class SnapshotSerializerSpec extends PekkoSpec {

  "Snapshot serializer" should {
    "deserialize akka snapshots" in {
      val system = ActorSystem()
      val serialization = SerializationExtension(system)
      // https://github.com/apache/incubator-pekko/pull/837#issuecomment-1847320309
      val data =
        "PAAAAAcAAABha2thLnBlcnNpc3RlbmNlLmZzbS5QZXJzaXN0ZW50RlNNJFBlcnNpc3RlbnRGU01TbmFwc2hvdAoPdGVzdC1pZGVudGlmaWVyEg0IFBIJdGVzdC1kYXRh"
      val bytes = Base64.getDecoder.decode(data)
      val result = serialization.deserialize(bytes, classOf[Snapshot])
      result.isSuccess shouldBe true
      val deserialized = result.get.data
      deserialized shouldBe a[Success[_]]
      val innerResult = deserialized.asInstanceOf[Success[_]].get
      innerResult shouldBe a[PersistentFSMSnapshot[_]]
      val persistentFSMSnapshot = innerResult.asInstanceOf[PersistentFSMSnapshot[_]]
      persistentFSMSnapshot shouldEqual PersistentFSMSnapshot[String]("test-identifier", "test-data", None)
    }
  }
}
