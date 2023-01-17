/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2017 Lightbend Inc. <https://www.lightbend.com>
 */
package akka.cluster.bootstrap

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.{ Cluster, ClusterEvent }
import akka.http.scaladsl.Http
import akka.management.scaladsl.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory

object DemoApp extends App {

  implicit val system = ActorSystem("simple")

  import system.log
  import system.dispatcher
  val cluster = Cluster(system)

  log.info("Started [{}], cluster.selfAddress = {}", system, cluster.selfAddress)

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  cluster
    .subscribe(system.actorOf(Props[ClusterWatcher]), ClusterEvent.InitialStateAsEvents, classOf[ClusterDomainEvent])

  import akka.http.scaladsl.server.Directives._
  Http().bindAndHandle(complete("Hello world"), "0.0.0.0", 8080)

}

class ClusterWatcher extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  override def receive = {
    case msg => log.info("Cluster {} >>> {}", msg, cluster.selfAddress)
  }
}
