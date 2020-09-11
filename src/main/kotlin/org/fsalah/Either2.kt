package org.fsalah

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.core.extensions.fx
import org.fsalah.Either2.BookingError
import org.fsalah.Either2.bookFlight
import org.fsalah.Either2.getSpeaker

object Either2 {

    sealed class BookingError {
        object MissingConference : BookingError()
        object CityNotFound : BookingError()
        object MissingTalk : BookingError()
        data class MissingSpeaker(val id: Int) : BookingError()

    }

    object City

    object Conference {
        fun getCity(): Either<BookingError, City> {
            return Right(City)
        }
    }

    object Talk {
        fun getConference(): Either<BookingError, Conference> {
            return Right(Conference)
        }
    }

    object Speaker {
        fun nextTalk(): Either<BookingError, Talk> {
            return Right(Talk)
        }
    }

    fun getSpeaker(id: Int): Either<BookingError, Speaker> {
        return Left(BookingError.MissingSpeaker(id))
    }

    fun bookFlight(city: City, speaker: Speaker, talk : Talk, conference: Conference) = "booked"
}


fun main() {
    Either.fx<BookingError, String> {
        val speaker = !getSpeaker(1)
        val talk = !speaker.nextTalk()
        val conference = !talk.getConference()
        val city = !conference.getCity()
        bookFlight(city , speaker, talk, conference)
    }.fold({ error ->
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