package org.fsalah

import arrow.core.Either
import arrow.core.flatMap
import org.fsalah.Either1.BookingError
import org.fsalah.Either1.processBooking


object Either1 {

    sealed class BookingError {
        object MissingConference : BookingError()
        object CityNotFound : BookingError()
        object MissingTalk : BookingError()
        data class MissingSpeaker(val id: Int) : BookingError()
    }

    object City

    object Conference {
        fun getCity(): Either<BookingError, City> {
            return Either.Right(City)
        }
    }

    object Talk {
        fun getConference(): Either<BookingError, Conference> {
            return Either.Right(Conference)
        }
    }

    object Speaker {
        fun nextTalk(): Either<BookingError, Talk> {
            return Either.Left(BookingError.MissingTalk)
        }
    }

    fun getSpeaker(id: Int): Either<BookingError, Speaker> {
        return Either.Left(BookingError.MissingSpeaker(id))
    }

    fun bookFlight(city: City, speaker: Speaker): String {
        return "booked"
    }

    fun processBooking(id: Int): Either<BookingError, String> {
        return getSpeaker(id)
                .flatMap { speaker ->
                    speaker.nextTalk()
                            .flatMap { talk -> talk.getConference() }
                            .flatMap { conference -> conference.getCity() }
                            .map { city -> bookFlight(city, speaker) }
                }
    }
}

fun main() {
     return processBooking(1)
            .fold({ error ->
                when (error) {
                    is BookingError.MissingConference -> println("no conference")
                    is BookingError.CityNotFound -> println("no city")
                    is BookingError.MissingTalk -> println("no talk")
                    is BookingError.MissingSpeaker -> println("no speaker for id ${error.id} ")
                }
            }, {
                println("you got this message \"$it\" ")
            })
}



