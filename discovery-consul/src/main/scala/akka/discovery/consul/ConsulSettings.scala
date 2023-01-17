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

package akka.discovery.consul

import akka.actor.ClassicActorSystemProvider
import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import akka.annotation.ApiMayChange

@ApiMayChange
final class ConsulSettings(system: ExtendedActorSystem) extends Extension {
  private val consulConfig = system.settings.config.getConfig("akka.discovery.akka-consul")

  val consulHost: String = consulConfig.getString("consul-host")

  val consulPort: Int = consulConfig.getInt("consul-port")

  val applicationNameTagPrefix: String = consulConfig.getString("application-name-tag-prefix")
  val applicationAkkaManagementPortTagPrefix: String =
    consulConfig.getString("application-akka-management-port-tag-prefix")
}

@ApiMayChange
object ConsulSettings extends ExtensionId[ConsulSettings] with ExtensionIdProvider {
  override def get(system: ActorSystem): ConsulSettings = super.get(system)

  override def get(system: ClassicActorSystemProvider): ConsulSettings = super.get(system)

  override def lookup: ConsulSettings.type = ConsulSettings

  override def createExtension(system: ExtendedActorSystem): ConsulSettings = new ConsulSettings(system)
}
