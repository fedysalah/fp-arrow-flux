package org.fsalah.services

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import reactor.core.publisher.Mono

data class Country(val code: String, val label: String)

data class CountryCode(val code: Option<String>)
data class Address(val id: Int, val country: Option<CountryCode> = Option.empty())
data class Person(val name: String, val address: Option<Address>)

class PersonService {

    private val personDB: Map<Int, Person> = mapOf(
            1 to Person(name = "Alfredo pizza café", address = Some(Address(id = 1))),
            2 to Person(name = "Pizza by Alfredo", address = Some(Address(id = 2)))
    )

    private val addressDB: Map<Int, Address> = mapOf(
            1 to Address(id = 1, country = Some(CountryCode(code = Some("ES")))),
            2 to Address(id = 2, country = None)
    )

    private val countryDB: Map<String, Country> = mapOf(
            "ES" to Country(code = "ES", label = "Spain")
    )


    fun findPerson(personId: Int): Mono<Option<Person>> =
            Mono.just(Option.fromNullable(personDB[personId]))


    fun listPersons(): Mono<List<Person>> =
            Mono.just(personDB.values.toList())

    fun findCountryCode(addressId: Int): Mono<Option<CountryCode>> =
            Mono.just(Option.fromNullable(addressDB[addressId]).flatMap { it.country })

    fun findCountry(code: String): Mono<Option<Country>> =
            Mono.just(Option.fromNullable(countryDB[code]))
}