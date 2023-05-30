/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2017-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.management.cluster.bootstrap.contactpoint

import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer
import java.util.function.BiFunction
import scala.concurrent.Future
import scala.concurrent.duration._
import org.apache.pekko
import pekko.actor.ActorSystem
import pekko.cluster.Cluster
import pekko.cluster.ClusterEvent.CurrentClusterState
import pekko.cluster.ClusterEvent.MemberUp
import pekko.discovery.Lookup
import pekko.discovery.MockDiscovery
import pekko.discovery.ServiceDiscovery.Resolved
import pekko.discovery.ServiceDiscovery.ResolvedTarget
import pekko.http.scaladsl.Http
import pekko.testkit.SocketUtil
import pekko.testkit.TestKit
import pekko.testkit.TestProbe
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import pekko.management.cluster.bootstrap.ClusterBootstrap
import org.scalactic.Tolerance
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Inside
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ClusterBootstrapDiscoveryBackoffIntegrationSpec extends AnyWordSpecLike with Matchers with Inside with Tolerance
    with ScalaFutures {

  "Cluster Bootstrap" should {

    var remotingPorts = Map.empty[String, Int]
    var contactPointPorts = Map.empty[String, Int]
    var unreachablePorts = Map.empty[String, Int]

    def config(id: String): Config = {
      val (managementPort, remotingPort, unreachablePort) = inside(
        SocketUtil.temporaryServerAddresses(3, "127.0.0.1").map(_.getPort)) {
        case Vector(mPort: Int, rPort: Int, uPort: Int) => (mPort, rPort, uPort)
        case o                                          => fail("Expected 3 ports but got: " + o)
      }

      info(s"System [$id]:  management port: $managementPort")
      info(s"System [$id]:    remoting port: $remotingPort")
      info(s"System [$id]: unreachable port: $unreachablePort")

      contactPointPorts = contactPointPorts.updated(id, managementPort)
      remotingPorts = remotingPorts.updated(id, remotingPort)
      unreachablePorts = unreachablePorts.updated(id, unreachablePort)

      ConfigFactory.parseString(s"""
        pekko {
          loglevel = INFO

          cluster.jmx.multi-mbeans-in-same-jvm = on

          # this can be referred to in tests to use the mock discovery implementation
          discovery.mock-dns.class = "org.apache.pekko.discovery.MockDiscovery"

          remote.netty.tcp.port = $remotingPort
          remote.artery.canonical.port = $remotingPort
          remote.artery.canonical.hostname = "127.0.0.1"

          management {
            http.management.port = $managementPort
            http.management.hostname = "127.0.0.1"
            cluster.bootstrap {
              contact-point-discovery {
                discovery-method = mock-dns

                service-namespace = "svc.cluster.local"

                stable-margin = 4 seconds

                interval = 500 ms

                port-name = "management"
              }
            }
          }
        }
        """.stripMargin).withFallback(ConfigFactory.load())
    }

    val systemName = "backoff-discovery-system"
    val systemA = ActorSystem(systemName, config("A"))
    val systemB = ActorSystem(systemName, config("B"))

    val clusterA = Cluster(systemA)
    val clusterB = Cluster(systemB)

    val bootstrapA = ClusterBootstrap(systemA)
    val bootstrapB = ClusterBootstrap(systemB)

    val baseTime = System.currentTimeMillis()
    case class DiscoveryRequest(time: Long, attempt: Int, res: Future[_]) {
      override def toString = s"DiscoveryRequest(${(time - baseTime).millis}, $attempt, $res)"
    }
    val resolveProbe = TestProbe()(systemA)

    // prepare the "mock DNS" - this resolves to unreachable addresses for the first three
    // times it is called, thus testing that discovery is called multiple times and
    // that formation will eventually succeed once discovery returns reachable addresses

    case class SystemStats(called: Int = 0, call2Timestamp: Long = 0, call3Timestamp: Long = 0)
    val perSystemStats = new ConcurrentHashMap[ActorSystem, SystemStats]()
    perSystemStats.put(systemA, SystemStats())
    perSystemStats.put(systemB, SystemStats())
    val name = s"$systemName.svc.cluster.local"

    MockDiscovery.set(
      Lookup(name).withProtocol("tcp").withPortName("management"),
      { system =>
        val stats = perSystemStats.compute(
          system,
          new BiFunction[ActorSystem, SystemStats, SystemStats] {
            override def apply(notUsed: ActorSystem, stats: SystemStats): SystemStats = {
              if (stats.called == 1)
                stats.copy(called = 2, call2Timestamp = System.nanoTime())
              else if (stats.called == 2)
                stats.copy(called = 3, call3Timestamp = System.nanoTime())
              else
                stats.copy(called = stats.called + 1)
            }
          })
        val res =
          if (stats.called < 4)
            Future.failed(new Exception("Boom! Discovery failed, was rate limited for example..."))
          else
            Future.successful(
              Resolved(
                name,
                List(
                  ResolvedTarget(
                    host = clusterA.selfAddress.host.get,
                    port = contactPointPorts.get("A"),
                    address = Option(InetAddress.getByName(clusterA.selfAddress.host.get))),
                  ResolvedTarget(
                    host = clusterB.selfAddress.host.get,
                    port = contactPointPorts.get("B"),
                    address = Option(InetAddress.getByName(clusterB.selfAddress.host.get))))))

        resolveProbe.ref ! DiscoveryRequest(System.currentTimeMillis(), stats.called, res)
        res

      })

    "start listening with the http contact-points on 2 systems" in {
      def start(system: ActorSystem, contactPointPort: Int) = {
        implicit val sys: ActorSystem = system

        val bootstrap: ClusterBootstrap = ClusterBootstrap(system)
        val routes = new HttpClusterBootstrapRoutes(bootstrap.settings).routes
        bootstrap.setSelfContactPoint(s"http://127.0.0.1:$contactPointPort")
        Http().newServerAt("127.0.0.1", contactPointPort).bind(routes)
      }

      start(systemA, contactPointPorts("A")).futureValue
      start(systemB, contactPointPorts("B")).futureValue
    }

    "poll discovery in exponentially increasing backoffs until eventually joining the returned nodes" in {
      bootstrapA.discovery shouldBe a[MockDiscovery]
      bootstrapA.discovery shouldBe a[MockDiscovery]

      bootstrapA.start()
      bootstrapB.start()

      val pA = TestProbe()(systemA)
      clusterA.subscribe(pA.ref, classOf[MemberUp])

      pA.expectMsgType[CurrentClusterState]
      val up1 = pA.expectMsgType[MemberUp](45.seconds)
      info("" + up1)

      perSystemStats.forEach {
        new BiConsumer[ActorSystem, SystemStats] {
          override def accept(system: ActorSystem, stats: SystemStats): Unit = {
            stats.called shouldBe >=(5)
            val durationBetweenCall2And3 = (stats.call3Timestamp - stats.call2Timestamp).nanos.toMillis
            info(s"${Cluster(system).selfAddress}: duration between call 2 and 3 $durationBetweenCall2And3 ms")
            durationBetweenCall2And3 shouldBe >=(
              (ClusterBootstrap(system).settings.contactPointDiscovery.interval * 2).toMillis)
          }
        }
      }
    }

    "terminate all systems" in {
      try TestKit.shutdownActorSystem(systemA, 3.seconds)
      finally TestKit.shutdownActorSystem(systemB, 3.seconds)
    }

  }

}
