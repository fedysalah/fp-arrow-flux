package org.fsalah.config

import org.fsalah.handlers.HealthCheck
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.web.reactive.function.server.router
import java.net.URI

class Router(healthCheck: HealthCheck) {
    val routes = router {
        accept(TEXT_HTML).nest {
            GET("/") { permanentRedirect(URI("../redoc")).build() }
            GET("/redoc") { permanentRedirect(URI("../redoc.html")).build() }
            GET("/health-check", healthCheck::healthCheck)
            resources("/**", ClassPathResource("/static/"))
        }
    }
}
