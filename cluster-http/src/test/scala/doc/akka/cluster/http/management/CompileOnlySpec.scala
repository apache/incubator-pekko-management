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

package doc.akka.cluster.http.management

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.http.scaladsl.server.Route
import akka.management.cluster.scaladsl.ClusterHttpManagementRoutes
import akka.management.scaladsl.AkkaManagement

object CompileOnlySpec {

  // #loading
  val system = ActorSystem()
  // Automatically loads Cluster Http Routes
  AkkaManagement(system).start()
  // #loading

  // #all
  val cluster = Cluster(system)
  val allRoutes: Route = ClusterHttpManagementRoutes(cluster)
  // #all

  // #read-only
  val readOnlyRoutes: Route = ClusterHttpManagementRoutes.readOnly(cluster)
  // #read-only
}
