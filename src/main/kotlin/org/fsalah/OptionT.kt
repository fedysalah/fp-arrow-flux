package org.fsalah

import arrow.core.Option
import arrow.core.Some
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.extensions.monok.monad.monad
import arrow.fx.reactor.fix
import arrow.mtl.OptionT
import arrow.mtl.extensions.fx
import arrow.mtl.extensions.optiont.monad.monad
import arrow.mtl.value
import org.fsalah.OptionT.processBooking
import org.fsalah.OptionT.processBookingT


object OptionT {

    val speakersDB: Map<Int, Speaker> = mapOf(
            1 to Speaker(name = "Fedy salah")
    )

    val confrencesDB: Map<Int, Conference> = mapOf(
            1 to Conference(name = "JugSummerCamp")
    )


    object City

    data class Talk(val id: Int)

    data class Conference(val name: String) {
        fun getCity(): Option<City> {
            return Some(City)
        }
    }

    data class Speaker(val name: String) {
        fun nextTalk(): Option<Talk> {
            return Some(Talk(1))
        }
    }

    fun bookFlight(city: City, speaker: Speaker) = "booked"

    fun processBooking(id: Int): MonoK<Option<MonoK<Option<String>>>> {
       return Repository.getSpeaker(id)
                .map { speakerOpt ->
                    speakerOpt.flatMap { speaker ->
                        speaker.nextTalk()
                                .map { talk ->
                                    Repository.getConference(talk.id)
                                            .map { conferenceOpt ->
                                                conferenceOpt.flatMap { conference ->
                                                    conference.getCity()
                                                }.map { city -> bookFlight(city, speaker) }
                                            }
                                }
                    }
                }
    }

    object Repository {
        fun getConference(id: Int): MonoK<Option<Conference>> {
            return MonoK.just(Option.fromNullable(confrencesDB[id]))
        }

        fun getSpeaker(id: Int): MonoK<Option<Speaker>> {
            return MonoK.just(Option.fromNullable(speakersDB[id]))
        }
    }

    fun processBookingT(id: Int): MonoK<Option<String>> {
        return OptionT.monad(MonoK.monad()).fx.monad {
            val speaker = !OptionT(Repository.getSpeaker(id))
            val talk = !OptionT(MonoK.just(speaker.nextTalk()))
            val conference = !OptionT(Repository.getConference(talk.id))
            val city = !OptionT(MonoK.just(conference.getCity()))
            bookFlight(city, speaker)
        }.value().fix()
    }

}

fun main() {
    processBookingT(1)
            .mono
            .map { bookingOpt ->
                bookingOpt.fold({
                    println("sorry !")
                },{
                    println("you got this message \"$it\" ")
                })
            }.subscribe()
}
