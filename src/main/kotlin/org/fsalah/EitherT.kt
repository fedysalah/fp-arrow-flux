package org.fsalah

import arrow.core.Either
import arrow.core.Option
import arrow.core.Right
import arrow.core.flatMap
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.extensions.monok.monad.monad
import arrow.fx.reactor.fix
import arrow.fx.reactor.k
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.fix
import arrow.mtl.value
import org.fsalah.EitherTJug.BookingError
import reactor.kotlin.core.publisher.toMono


object EitherTJug {

    sealed class BookingError {
        object MissingConference : BookingError()
        data class MissingSpeaker(val id: Int) : BookingError()
    }

    val speakersDB: Map<Int, Speaker> = mapOf(
            1 to Speaker(name = "Fedy salah")
    )

    val confrencesDB: Map<Int, Conference> = mapOf(
            2 to Conference(name = "JugSummerCamp")
    )

    object City

    data class Talk(val id: Int)

    data class Conference(val name: String) {
        fun getCity(): Either<BookingError, City> {
            return Right(City)
        }
    }

    data class Speaker(val name: String) {
        fun nextTalk(): Either<BookingError, Talk> {
            return Right(Talk(1))
        }
    }

    object Repository {
        fun getConference(id: Int): MonoK<Either<BookingError, Conference>> {
            return MonoK.just(Option.fromNullable(confrencesDB[id]).toEither { BookingError.MissingConference })
        }

        fun getSpeaker(id: Int): MonoK<Either<BookingError, Speaker>> {
            return MonoK.just(Option.fromNullable(speakersDB[id]).toEither { BookingError.MissingSpeaker(id) })
        }
    }

    fun bookFlight(city: City, speaker: Speaker) = "booked"

    fun processBooking(id: Int): MonoK<Either<BookingError, MonoK<Either<BookingError, String>>>> {
        return Repository.getSpeaker(id)
                .map { speakerOrError ->
                    speakerOrError.flatMap { speaker ->
                        speaker.nextTalk()
                                .map { talk ->
                                    Repository.getConference(talk.id)
                                            .map { conferenceOrError ->
                                                conferenceOrError.flatMap { conference ->
                                                    conference.getCity()
                                                }.map { city ->
                                                    bookFlight(city, speaker)
                                                }
                                            }
                                }
                    }
                }
    }

    fun processBookingT(id: Int): MonoK<Either<BookingError, String>> {
       return EitherT.monad<BookingError, ForMonoK>(MonoK.monad()).fx.monad {
           val speaker = !EitherT(Repository.getSpeaker(id))
           val talk = !EitherT(speaker.nextTalk().toMono().k())
           val conference = !EitherT(Repository.getConference(talk.id))
           val city = !EitherT(conference.getCity().toMono().k())
           bookFlight(city, speaker)
       }.value().fix()
    }
}

fun main() {
    EitherTJug.processBookingT(1)
            .mono
            .subscribe { next ->
                next.fold({
                    error ->  when (error) {
                        is BookingError.MissingConference -> println("no conference")
                        is BookingError.MissingSpeaker -> println("no speaker for id ${error.id} ")
                    }
                }, {
                    println(" got message $it")
                })
            }
}
