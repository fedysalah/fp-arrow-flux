package org.fsalah

import arrow.core.Either
import arrow.core.Right


object EitherJug {

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
        return Right(Speaker)
    }

    fun bookFlight(city: City, speaker: Speaker, talk: Talk, conference: Conference) = "booked all"

    fun bookFlight(city: City, speaker: Speaker) = "booked for speaker"

    fun bookFlight(city: City) = "booked"

}