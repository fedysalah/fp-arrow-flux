package org.fsalah

import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.fx
import org.fsalah.Option3.bookFlight
import org.fsalah.Option3.getSpeaker

object Option3 {
    object City

    object Conference {
        fun getCity(): Option<City> {
            return Some(City)
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

    fun getSpeaker(): Option<Speaker> {
        return Some(Speaker)
    }

    fun bookFlight(city: City, speaker: Speaker, talk : Talk, conference: Conference) = "booked"

}


fun main() {
    Option.fx {
        val speaker = !getSpeaker()
        val talk = !speaker.nextTalk()
        val conference = !talk.getConference()
        val city = !conference.getCity()
        bookFlight(city, speaker, talk, conference)
    }.fold({
        println("sorry !")
    }, {
        println("you got this message \"$it\" ")
    })
}



