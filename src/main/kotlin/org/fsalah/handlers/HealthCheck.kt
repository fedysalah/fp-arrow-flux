package org.fsalah.handlers

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono

class HealthCheck {
    fun healthCheck(request: ServerRequest) : Mono<ServerResponse> {
        return ok().build()
    }
}
