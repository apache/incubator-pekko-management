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

package akka.cluster.bootstrap.demo;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.server.AllDirectives;
//#start-akka-management
import akka.management.javadsl.AkkaManagement;

//#start-akka-management
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.stream.Materializer;

public class DemoApp extends AllDirectives {

  DemoApp() {
    ActorSystem system = ActorSystem.create("Appka");

    Materializer mat = Materializer.createMaterializer(system);
    Cluster cluster = Cluster.get(system);

    system.log().info("Started [" + system + "], cluster.selfAddress = " + cluster.selfAddress() + ")");

    //#start-akka-management
    AkkaManagement.get(system).start();
    //#start-akka-management
    ClusterBootstrap.get(system).start();

    cluster
      .subscribe(system.actorOf(Props.create(ClusterWatcher.class)), ClusterEvent.initialStateAsEvents(), ClusterEvent.ClusterDomainEvent.class);

    Http.get(system).bindAndHandle(complete("Hello world").flow(system, mat), ConnectHttp.toHost("0.0.0.0", 8080), mat);

    cluster.registerOnMemberUp(() -> {
      system.log().info("Cluster member is up!");
    });
  }

  public static void main(String[] args) {
    new DemoApp();
  }
}

