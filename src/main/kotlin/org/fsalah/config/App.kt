package org.fsalah.config

import org.fsalah.handlers.HealthHandler
import org.fsalah.handlers.PersonHandler
import org.fsalah.services.PersonService
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.server.adapter.WebHttpHandlerBuilder

object App {
    fun register() = beans {
        routes(this)
    }

    private fun routes(ctx: BeanDefinitionDsl) = with (ctx) {
        bean(WebHttpHandlerBuilder.WEB_HANDLER_BEAN_NAME) {
            RouterFunctions.toWebHandler(ref<Router>().routes, HandlerStrategies.withDefaults())
        }
        bean<Router>()
        bean<HealthHandler>()
        bean<PersonHandler>()
        bean<PersonService>()
    }
}
