/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2020-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.remote.artery.tcp.ssl

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
 */
class X509ReadersSpec extends AnyWordSpec with Matchers {
  import TlsResourcesSpec._

  "X509Readers" must {
    "read a certificate's name from the CN" in {
      val island = loadCert("/ssl/island.example.com.crt")
      X509Readers.getAllSubjectNames(island) mustBe (Set("island.example.com"))
    }

    "read both the CN and the subject alternative names" in {
      val serverCert = loadCert("/domain.crt")
      X509Readers.getAllSubjectNames(serverCert) mustBe (Set("pekko-remote", "localhost"))
    }

    "read a certificate that has no SAN extension" in {
      // a self-signed CA without SAN
      val island = loadCert("/ssl/pem/selfsigned-certificate.pem")
      X509Readers.getAllSubjectNames(island) mustBe (Set("0d207b68-9a20-4ee8-92cb-bf9699581cf8"))
    }

  }

}
