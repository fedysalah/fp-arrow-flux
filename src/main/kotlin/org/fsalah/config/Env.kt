package org.fsalah.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object Env {
    private val config: Config = ConfigFactory.load()
    private val server: Config = config.getConfig("server")
    val port = server.getInt("port")
    val shutdownTimeout = server.getLong("shutdown-timeout")
}
