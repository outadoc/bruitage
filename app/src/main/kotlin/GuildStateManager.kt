package fr.outadoc.bruitage.app

import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.on
import java.util.concurrent.ConcurrentHashMap

object GuildStateManager {
    private val states = ConcurrentHashMap<ULong, GuildState>()

    fun get(guildId: ULong): GuildState? = states[guildId]

    fun getOrCreate(
        guildId: ULong,
        lavalink: LavaKord,
        onTrackEnd: suspend (ULong) -> Unit,
    ): GuildState =
        states.getOrPut(guildId) {
            val link = lavalink.getLink(guildId)
            link.player.on<TrackEndEvent> { onTrackEnd(guildId) }
            GuildState(link)
        }
}
