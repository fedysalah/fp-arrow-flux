package org.fsalah

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.fx
import arrow.core.nel

object JugOption {

    object City

    object Conference {
        fun getCity(): Option<City> = Some(City)
    }

    object Talk {
        fun getConference(): Option<Conference> = Some(Conference)
    }

    object Speaker {
        fun nextTalk(): Option<Talk> = None
    }

    fun getSpeaker(id: Int): Option<Speaker> = Some(Speaker)

    fun bookFlight(city: City): String = "booked"

    fun bookFlight(city: City, speaker: Speaker): String = "booked for speaker"


    fun processFlight(id: Int): Option<String> {
        return getSpeaker(1)
                .flatMap { sp -> sp.nextTalk() }
                .flatMap { talk -> talk.getConference() }
                .flatMap { conference -> conference.getCity() }
                .map { city ->
                    bookFlight(city)
                }
    }

    fun processFlightFx(id: Int): Option<String> {
     return Option.fx {
          val speaker = !getSpeaker(id)
          val talk = !speaker.nextTalk()
          val conference = !talk.getConference()
          val city = !conference.getCity()
          bookFlight(city, speaker)
      }
    }

}


fun main() {
    JugOption.processFlightFx(1)
            .fold({
                println("sorry ! ")
            }, {
                println("you got message : $it")
            })
}
