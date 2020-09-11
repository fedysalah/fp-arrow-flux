package org.fsalah

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import org.fsalah.Option1.bookFlight
import org.fsalah.Option1.processBooking

object Option1 {

    object City

    object Conference {
        fun getCity(): Option<City> {
            return None
        }
    }

    object Talk {
        fun getConference(): Option<Conference> {
            return Some(Conference)
        }
    }

    object Speaker {
        fun nextTalk(): Option<Talk> {
            return Some(Talk)
        }
    }

    fun getSpeaker(id: Int): Option<Speaker> {
        return Some(Speaker)
    }

    fun bookFlight(city: City): String {
        return "booked"
    }

    fun processBooking(id: Int): Option<String> {
        return getSpeaker(id)
                .flatMap { speaker -> speaker.nextTalk() }
                .flatMap { talk -> talk.getConference() }
                .flatMap { conference -> conference.getCity() }
                .map { city -> bookFlight (city) }
    }
}


fun main() {
    processBooking(1)
            .fold({
                println("sorry !")
            }, {
                println("you got this message \"$it\" ")
            })
}



