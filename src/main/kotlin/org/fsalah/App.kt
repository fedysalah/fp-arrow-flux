package org.fsalah

import org.fsalah.config.App.register
import org.fsalah.config.Env
import org.springframework.context.support.GenericApplicationContext
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import reactor.netty.http.server.HttpServer
import java.time.Duration

object App {
    private val httpHandler: HttpHandler
    private val server: HttpServer

    init {
        val context = GenericApplicationContext()
                .apply {
                    register().initialize(this)
                    refresh()
                }
        server = HttpServer.create().port(Env.port)
        httpHandler = WebHttpHandlerBuilder.applicationContext(context).build()
    }

    fun startAndAwait() {
        server.handle(ReactorHttpHandlerAdapter(httpHandler))
                .bindUntilJavaShutdown(Duration.ofSeconds(Env.shutdownTimeout)) {
                    logger.info("Server Started")
                }
    }
}

fun main() {
    App.startAndAwait()
}
