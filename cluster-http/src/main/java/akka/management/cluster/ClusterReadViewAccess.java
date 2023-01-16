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

package akka.management.cluster;

import akka.annotation.InternalApi;
import akka.cluster.Cluster;
import akka.cluster.ClusterReadView;

/** INTERNAL API */
@InternalApi
public class ClusterReadViewAccess {

  /**
   * INTERNAL API
   *
   * Exposes the internal {@code readView} of the Akka Cluster, not reachable from Scala code because it is {@code private[cluster]}.
   */
  @InternalApi
  public static ClusterReadView internalReadView(Cluster cluster) {
    return cluster.readView();
  }
}
