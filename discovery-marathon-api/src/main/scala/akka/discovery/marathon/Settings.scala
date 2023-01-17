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

package akka.discovery.marathon

import akka.actor._
import akka.annotation.ApiMayChange

@ApiMayChange
final class Settings(system: ExtendedActorSystem) extends Extension {
  private val marathonApi = system.settings.config.getConfig("akka.discovery.marathon-api")

  val appApiUrl: String =
    marathonApi.getString("app-api-url")

  val appPortName: String =
    marathonApi.getString("app-port-name")

  val appLabelQuery: String =
    marathonApi.getString("app-label-query")
}

@ApiMayChange
object Settings extends ExtensionId[Settings] with ExtensionIdProvider {
  override def get(system: ActorSystem): Settings = super.get(system)

  override def get(system: ClassicActorSystemProvider): Settings = super.get(system)

  override def lookup: Settings.type = Settings

  override def createExtension(system: ExtendedActorSystem): Settings = new Settings(system)
}
