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

package org.apache.pekko.management.http;

import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.management.cluster.ClusterHttpManagementRouteProvider;
import org.junit.Test;

public class ClusterHttpManagementJavaCompileTest {

  public void test() {
    ActorSystem actorSystem = ActorSystem.create("test");
    ClusterHttpManagementRouteProvider x = ClusterHttpManagementRouteProvider.get(actorSystem);
  }

  @Test
  public void compileOnly() {}
}
