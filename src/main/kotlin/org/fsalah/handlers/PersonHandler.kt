package org.fsalah.handlers

import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.fx
import arrow.core.getOrElse
import arrow.core.toOption
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.extensions.fx
import arrow.fx.reactor.extensions.monok.applicativeError.handleErrorWith
import arrow.fx.reactor.extensions.monok.monad.flatMap
import arrow.fx.reactor.extensions.monok.monad.monad
import arrow.fx.reactor.fix
import arrow.fx.reactor.k
import arrow.mtl.OptionT
import arrow.mtl.extensions.fx
import org.fsalah.services.Address
import org.fsalah.services.Country
import org.fsalah.services.CountryCode
import org.fsalah.services.Person
import org.fsalah.services.PersonService
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class PersonHandler(
        private val personService: PersonService
) {

    fun getPerson(request: ServerRequest): Mono<ServerResponse> {
        val personId: Option<Int> = request.pathVariable("personId").toIntOrNull().toOption()
        return personId.fold({
            ServerResponse.notFound().build()
        }, { id ->
            personService.findPerson(id)
                    .flatMap { personOpt ->
                        personOpt.fold({
                            ServerResponse.notFound().build()
                        }, { person ->
                            ServerResponse.ok().bodyValue(person)
                        })
                    }
        })
    }

    fun getPersonMonad(request: ServerRequest): Mono<out ServerResponse> {
        return OptionT.fx(MonoK.monad()) {
            val personId = !OptionT(MonoK.just(request.pathVariable("personId").toIntOrNull().toOption()))
            !OptionT(personService.findPerson(personId).k())
        }.value().flatMap { personOpt ->
            personOpt.fold({
                ServerResponse.notFound().build().k()
            }, { person ->
                ServerResponse.ok().bodyValue(person).k()
            })
        }.mono
    }


    fun getPersonCountry(request: ServerRequest): Mono<out ServerResponse> {
        val personId: Option<Int> = request.pathVariable("personId").toIntOrNull().toOption()
        return personId.fold({
            ServerResponse.notFound().build()
        }, { id ->
            personService.findPerson(id).flatMap { maybePerson ->
                maybePerson.flatMap { person -> person.address }
                        .map { address ->
                            personService.findCountryCode(address.id).flatMap { maybeCountry ->
                                maybeCountry.flatMap { country -> country.code }
                                        .map { code ->
                                            personService.findCountry(code)
                                        }.getOrElse {
                                            Mono.just(Option.empty())
                                        }
                            }
                        }.getOrElse {
                            Mono.just(Option.empty())
                        }
            }.flatMap { codeOpt ->
                codeOpt.fold({
                    ServerResponse.notFound().build()
                }, { code ->
                    ServerResponse.ok().bodyValue(code)
                })
            }
        })
    }


    fun getPersonCountryMonad(request: ServerRequest): Mono<out ServerResponse> {
        return MonoK.fx {
            val personId = request.pathVariable("personId").toIntOrNull().toOption().fold(
                    { MonoK.raiseError<Int>(NoSuchElementException("...")) },
                    { MonoK.just(it) }
            ).bind()
            val maybePerson = personService.findPerson(personId).k().bind()
            val person = maybePerson.fold(
                    { MonoK.raiseError<Person>(NoSuchElementException("...")) },
                    { MonoK.just(it) }
            ).bind()

            val address = person.address.fold(
                    { MonoK.raiseError<Address>(NoSuchElementException("...")) },
                    { MonoK.just(it) }
            ).bind()
            val maybeCountryCode = personService.findCountryCode(address.id).k().bind()

            val countryCode = maybeCountryCode.fold(
                    { MonoK.raiseError<CountryCode>(NoSuchElementException("...")) },
                    { MonoK.just(it) }
            ).bind()

            val code = countryCode.code.fold(
                    { MonoK.raiseError<String>(NoSuchElementException("...")) },
                    { MonoK.just(it) }
            ).bind()
            val countryOpt = personService.findCountry(code).k().bind()
            val country = countryOpt.fold(
                    { MonoK.raiseError<Country>(NoSuchElementException("...")) },
                    { MonoK.just(it) }
            ).bind()
            country
        }.fix().flatMap { country ->
            ServerResponse.ok().bodyValue(country).k()
        }.handleErrorWith {
            ServerResponse.notFound().build().k()
        }.mono
    }

    fun getPersonCountryMonadT(request: ServerRequest): Mono<out ServerResponse> {
        return OptionT.fx(MonoK.monad()) {
            val personId = !OptionT(MonoK.just(request.pathVariable("personId").toIntOrNull().toOption()))
            val person = !OptionT(personService.findPerson(personId).k())
            val address = !OptionT(MonoK.just(person.address))
            val countryCode = !OptionT(personService.findCountryCode(address.id).k())
            val code = !OptionT(MonoK.just(countryCode.code))
            val country = !OptionT(personService.findCountry(code).k())
            country
        }.value().flatMap { codeOpt ->
            codeOpt.fold({
                ServerResponse.notFound().build().k()
            }, { code ->
                ServerResponse.ok().bodyValue(code).k()
            })
        }.mono
    }
}