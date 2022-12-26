/*
 * Copyright (C) 2019-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package doc.org.apache.pekko.serialization.jackson.v1

import doc.org.apache.pekko.serialization.jackson.MySerializable

// #structural
case class Customer(name: String, street: String, city: String, zipCode: String, country: String) extends MySerializable
// #structural
