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

package org.apache.pekko.discovery.kubernetes

import spray.json._
import scala.io.Source

import PodList._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class JsonFormatSpec extends AnyWordSpec with Matchers {
  "JsonFormat" should {
    val data = resourceAsString("pods.json")

    "work" in {
      JsonFormat.podListFormat.read(data.parseJson) shouldBe PodList(
        List(
          Pod(
            Some(PodSpec(List(Container(
              "pekko-cluster-tooling-example",
              Some(List(
                ContainerPort(Some("pekko-remote"), 10000),
                ContainerPort(Some("management"), 10001),
                ContainerPort(Some("http"), 10002))))))),
            Some(
              PodStatus(
                Some("172.17.0.4"),
                Some(List(ContainerStatus("pekko-cluster-tooling-example", Map(("running", ()))))),
                Some("Running"))),
            Some(Metadata(deletionTimestamp = None))),
          Pod(
            Some(PodSpec(List(Container(
              "pekko-cluster-tooling-example",
              Some(List(
                ContainerPort(Some("pekko-remote"), 10000),
                ContainerPort(Some("management"), 10001),
                ContainerPort(Some("http"), 10002))))))),
            Some(
              PodStatus(
                Some("172.17.0.6"),
                Some(List(ContainerStatus("pekko-cluster-tooling-example", Map(("running", ()))))),
                Some("Running"))),
            Some(Metadata(deletionTimestamp = None))),
          Pod(
            Some(PodSpec(List(Container(
              "pekko-cluster-tooling-example",
              Some(List(
                ContainerPort(Some("pekko-remote"), 10000),
                ContainerPort(Some("management"), 10001),
                ContainerPort(Some("http"), 10002))))))),
            Some(
              PodStatus(
                Some("172.17.0.7"),
                Some(List(ContainerStatus("pekko-cluster-tooling-example", Map(("running", ()))))),
                Some("Running"))),
            Some(Metadata(deletionTimestamp = Some("2017-12-06T16:30:22Z")))),
          Pod(
            Some(PodSpec(
              List(Container("pekko-cluster-tooling-example", Some(List(ContainerPort(Some("management"), 10001))))))),
            Some(
              PodStatus(
                Some("172.17.0.47"),
                Some(List(ContainerStatus("pekko-cluster-tooling-example", Map(("terminated", ()))))),
                Some("Succeeded"))),
            Some(Metadata(deletionTimestamp = None)))))
    }
  }

  private def resourceAsString(name: String): String =
    Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(name)).mkString
}
