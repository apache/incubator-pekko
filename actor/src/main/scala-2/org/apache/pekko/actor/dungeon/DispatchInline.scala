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

package org.apache.pekko.actor.dungeon

import org.apache.pekko
import pekko.actor.ActorCell
import pekko.annotation.InternalApi
import pekko.dispatch.Mailbox
import pekko.util.Unsafe

@InternalApi
private[pekko] trait DispatchInline { this: ActorCell =>
  @inline final def mailbox: Mailbox =
    Unsafe.instance.getObjectVolatile(this, AbstractActorCell.mailboxOffset).asInstanceOf[Mailbox]

}
