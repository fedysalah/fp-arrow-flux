package org.fsalah

import arrow.fx.Schedule.Companion.delay
import arrow.fx.extensions.io.concurrent.sleep
import org.fsalah.config.App.register
import org.fsalah.config.Env
import org.springframework.context.support.GenericApplicationContext
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.server.adapter.WebHttpHandlerBuilder
import reactor.netty.http.server.HttpServer
import java.io.IOException
import java.lang.RuntimeException
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Future

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
    try {
        treatment1()
        println("hello from ${Thread.currentThread().name}")
        treatment2()

        println("hello from ${Thread.currentThread().name}")
    } catch (t: RuntimeException) {
        println("got error...")
    }
    Thread.sleep(5000)
    println("bye!")
    //App.startAndAwait()
}

fun treatment1(): Thread {
    return Thread {
        Thread.sleep(1000)
        throw RuntimeException()
        println("hello from ${Thread.currentThread().name}")
    }

}

fun treatment2() {
    return Thread {
        Thread.sleep(2000)
        println("hello from ${Thread.currentThread().name}")
    }.start()
}