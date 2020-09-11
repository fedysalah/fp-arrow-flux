package org.fsalah.handlers

import org.fsalah.validation.Config
import org.fsalah.validation.ConnectionParams
import org.fsalah.validation.Read
import org.fsalah.validation.parallelValidate
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono

class HealthHandler {
    fun healthCheck(request: ServerRequest): Mono<ServerResponse> {

        val config = Config(mapOf("url" to "127.0.0.1", "port" to "1337"))

        val valid = parallelValidate(
                config.parse(Read.stringRead, "url"),
                config.parse(Read.intRead, "port")
        ) { url, port -> ConnectionParams(url, port) }

        return ok().build()
    }
}
