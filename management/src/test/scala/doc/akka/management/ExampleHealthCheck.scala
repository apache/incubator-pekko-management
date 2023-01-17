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

package doc.akka.management

import akka.actor.ActorSystem
import akka.cluster.{ Cluster, MemberStatus }

import scala.concurrent.Future

//#basic
class ExampleHealthCheck(system: ActorSystem) extends (() => Future[Boolean]) {
  override def apply(): Future[Boolean] = {
    Future.successful(true)
  }
}
//#basic

//#cluster
class ClusterHealthCheck(system: ActorSystem) extends (() => Future[Boolean]) {
  private val cluster = Cluster(system)
  override def apply(): Future[Boolean] = {
    Future.successful(cluster.selfMember.status == MemberStatus.Up)
  }
}
//#cluster
