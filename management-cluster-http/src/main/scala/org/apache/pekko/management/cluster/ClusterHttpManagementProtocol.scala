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

package org.apache.pekko.management.cluster

import org.apache.pekko
import pekko.annotation.InternalApi
import pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

import scala.collection.immutable

final case class ClusterUnreachableMember(node: String, observedBy: immutable.Seq[String])
final case class ClusterMember(node: String, nodeUid: String, status: String, roles: Set[String])
object ClusterMember {
  implicit val clusterMemberOrdering: Ordering[ClusterMember] = Ordering.by(_.node)
}
final case class ClusterMembers(
    selfNode: String,
    members: Set[ClusterMember],
    unreachable: immutable.Seq[ClusterUnreachableMember],
    leader: Option[String],
    oldest: Option[String],
    oldestPerRole: Map[String, String])
final case class ClusterHttpManagementMessage(message: String)
final case class ShardEntityTypeKeys(entityTypeKeys: immutable.Set[String])
final case class ShardRegionInfo(shardId: String, numEntities: Int)
final case class ShardDetails(regions: immutable.Seq[ShardRegionInfo])

/** INTERNAL API */
@InternalApi private[pekko] sealed trait ClusterHttpManagementMemberOperation

/** INTERNAL API */
@InternalApi private[pekko] case object Down extends ClusterHttpManagementMemberOperation

/** INTERNAL API */
@InternalApi private[pekko] case object Leave extends ClusterHttpManagementMemberOperation

/** INTERNAL API */
@InternalApi private[pekko] case object Join extends ClusterHttpManagementMemberOperation

/** INTERNAL API */
@InternalApi private[pekko] object ClusterHttpManagementMemberOperation {
  def fromString(value: String): Option[ClusterHttpManagementMemberOperation] =
    Vector(Down, Leave, Join).find(_.toString.equalsIgnoreCase(value))
}

trait ClusterHttpManagementJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val clusterUnreachableMemberFormat: RootJsonFormat[ClusterUnreachableMember] =
    jsonFormat2(ClusterUnreachableMember.apply)
  implicit val clusterMemberFormat: RootJsonFormat[ClusterMember] = jsonFormat4(ClusterMember.apply)
  implicit val clusterMembersFormat: RootJsonFormat[ClusterMembers] = jsonFormat6(ClusterMembers.apply)
  implicit val clusterMemberMessageFormat: RootJsonFormat[ClusterHttpManagementMessage] =
    jsonFormat1(ClusterHttpManagementMessage.apply)
  implicit val shardEntityTypeKeysFormat: RootJsonFormat[ShardEntityTypeKeys] = jsonFormat1(ShardEntityTypeKeys.apply)
  implicit val shardRegionInfoFormat: RootJsonFormat[ShardRegionInfo] = jsonFormat2(ShardRegionInfo.apply)
  implicit val shardDetailsFormat: RootJsonFormat[ShardDetails] = jsonFormat1(ShardDetails.apply)
}
