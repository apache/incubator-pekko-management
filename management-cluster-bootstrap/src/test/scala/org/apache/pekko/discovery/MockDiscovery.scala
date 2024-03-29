/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2017-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.discovery

import java.util.concurrent.atomic.AtomicReference
import org.apache.pekko
import pekko.actor.ActorSystem
import pekko.annotation.InternalApi
import pekko.discovery.ServiceDiscovery.Resolved
import pekko.event.{ LogSource, Logging }

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

@InternalApi
object MockDiscovery {
  private val data = new AtomicReference[Map[Lookup, ActorSystem => Future[Resolved]]](Map.empty)

  def set(name: Lookup, to: () => Future[Resolved]): Unit =
    set(name, _ => to())

  @tailrec
  def set(name: Lookup, to: ActorSystem => Future[Resolved]): Unit = {
    val d = data.get()
    if (data.compareAndSet(d, d.updated(name, to))) ()
    else set(name, to) // retry
  }

  @tailrec
  def remove(name: Lookup): Unit = {
    val d = data.get()
    if (data.compareAndSet(d, d - name)) ()
    else remove(name) // retry
  }
}

@InternalApi
final class MockDiscovery(system: ActorSystem) extends ServiceDiscovery {

  private val log = Logging(system, getClass)(LogSource.fromClass)

  override def lookup(query: Lookup, resolveTimeout: FiniteDuration): Future[Resolved] = {
    MockDiscovery.data.get().get(query) match {
      case Some(res) =>
        val items = res(system)
        log.info("Mock-resolved [{}] to [{}:{}]", query, items, items.value)
        items
      case None =>
        log.info("No mock-data for [{}], resolving as 'Nil'. Current mocks: {}", query, MockDiscovery.data.get())
        Future.successful(Resolved(query.serviceName, Nil))
    }
  }

}
