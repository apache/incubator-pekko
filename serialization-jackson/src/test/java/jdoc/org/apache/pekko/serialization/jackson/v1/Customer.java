/*
 * Copyright (C) 2019-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package jdoc.org.apache.pekko.serialization.jackson.v1;

import jdoc.org.apache.pekko.serialization.jackson.MySerializable;

// #structural
public class Customer implements MySerializable {
  public final String name;
  public final String street;
  public final String city;
  public final String zipCode;
  public final String country;

  public Customer(String name, String street, String city, String zipCode, String country) {
    this.name = name;
    this.street = street;
    this.city = city;
    this.zipCode = zipCode;
    this.country = country;
  }
}
// #structural
