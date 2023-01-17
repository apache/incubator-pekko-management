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

import akka.actor.ActorSystem
import akka.cluster.{ Cluster, MemberStatus }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.management.scaladsl.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap

object DemoApp extends App {
  implicit val system = ActorSystem("my-system")

  val cluster = Cluster(system)

  def isReady() = {
    val selfNow = cluster.selfMember

    selfNow.status == MemberStatus.Up
  }

  def isHealthy() = {
    isReady()
  }

  val route =
    concat(
      path("ping")(complete("pong!")),
      path("healthy")(complete(if (isHealthy()) StatusCodes.OK else StatusCodes.ServiceUnavailable)),
      path("ready")(complete(if (isReady()) StatusCodes.OK else StatusCodes.ServiceUnavailable)))

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  Http().bindAndHandle(
    route,
    sys.env.get("HOST").getOrElse("127.0.0.1"),
    sys.env.get("PORT_HTTP").map(_.toInt).getOrElse(8080))
}
