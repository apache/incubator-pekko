/*
 * Copyright (C) 2019-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package jdoc.org.apache.pekko.serialization.jackson.v2b;

import jdoc.org.apache.pekko.serialization.jackson.MySerializable;

// #add-mandatory
public class ItemAdded implements MySerializable {
  public final String shoppingCartId;
  public final String productId;
  public final int quantity;
  public final double discount;

  public ItemAdded(String shoppingCartId, String productId, int quantity, double discount) {
    this.shoppingCartId = shoppingCartId;
    this.productId = productId;
    this.quantity = quantity;
    this.discount = discount;
  }
}
// #add-mandatory
