package org.fsalah.config

import org.fsalah.handlers.HealthHandler
import org.fsalah.handlers.PersonHandler
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.web.reactive.function.server.router
import java.net.URI

class Router(
        healthHandler: HealthHandler,
        personHandler: PersonHandler
) {
    val routes = router {
        accept(TEXT_HTML).nest {
            GET("/") { permanentRedirect(URI("../redoc")).build() }
            GET("/redoc") { permanentRedirect(URI("../redoc.html")).build() }
            GET("/health-check", healthHandler::healthCheck)
            resources("/**", ClassPathResource("/static/"))
            (path("/persons") and accept(MediaType.APPLICATION_JSON)).nest {
                GET("/{personId}", personHandler::getPersonMonad)
                GET("/{personId}/country", personHandler::getPersonCountryMonadT)
            }
        }
    }
}
