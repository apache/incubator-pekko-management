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

package org.apache.pekko.coordination.lease.kubernetes

import org.apache.pekko
import pekko.actor.ActorSystem
import pekko.coordination.lease.TimeoutSettings
import pekko.coordination.lease.kubernetes.internal.KubernetesApiImpl
import pekko.coordination.lease.scaladsl.LeaseProvider
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.time.{ Milliseconds, Seconds, Span }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
 * This spec is for running inside a k8s cluster.
 */
abstract class LeaseSpec() extends AnyWordSpec with ScalaFutures with BeforeAndAfterAll with Matchers with Eventually {

  def system: ActorSystem

  implicit val patience: PatienceConfig = PatienceConfig(Span(3, Seconds), Span(500, Milliseconds))

  lazy val underTest = LeaseProvider(system)
  // for cleanup
  val config = system.settings.config.getConfig(KubernetesLease.configPath)
  lazy val k8sApi = new KubernetesApiImpl(system, KubernetesSettings(config, TimeoutSettings(config)))
  val leaseName = "lease-1"
  val client1 = "client1"
  val client2 = "client2"

  // two leases instances for the same lease name
  lazy val lease1Client1 = underTest.getLease(leaseName, "pekko.coordination.lease.kubernetes", client1)
  lazy val lease1Client2 = underTest.getLease(leaseName, "pekko.coordination.lease.kubernetes", client2)

  "A lease" should {

    "be different instances" in {
      assert(lease1Client1 ne lease1Client2)
    }

    "work" in {
      lease1Client1.acquire().futureValue shouldEqual true
      lease1Client1.checkLease() shouldEqual true
    }

    "be reentrant" in {
      lease1Client1.acquire().futureValue shouldEqual true
      lease1Client1.checkLease() shouldEqual true
      lease1Client2.checkLease() shouldEqual false
    }

    "not allow another client to acquire the lease" in {
      lease1Client2.acquire().futureValue shouldEqual false
      lease1Client2.checkLease() shouldEqual false
    }

    "maintain the lease for a prolonged period" in {
      lease1Client1.acquire().futureValue shouldEqual true
      lease1Client1.checkLease() shouldEqual true
      Thread.sleep(200)
      lease1Client1.checkLease() shouldEqual true
      Thread.sleep(200)
      lease1Client1.checkLease() shouldEqual true
      Thread.sleep(200)
      lease1Client1.checkLease() shouldEqual true
    }

    "not allow another client to release the lease" in {
      lease1Client2.release().failed.futureValue.getMessage shouldEqual s"Tried to release a lease that is not acquired"
    }

    "allow removing the lease" in {
      lease1Client1.release().futureValue shouldEqual true
      eventually {
        lease1Client1.checkLease() shouldEqual false
      }
    }

    "allow a new client to get the lease once released" in {
      lease1Client2.acquire().futureValue shouldEqual true
      lease1Client2.checkLease() shouldEqual true
      lease1Client1.checkLease() shouldEqual false
    }
  }

}
