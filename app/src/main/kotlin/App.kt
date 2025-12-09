package fr.outadoc.bruitage.app

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.component.ActionRowComponent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder
import dev.kord.rest.builder.component.MessageComponentBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.lavakord.plugins.sponsorblock.Sponsorblock
import dev.schlaubi.lavakord.plugins.sponsorblock.model.Category
import dev.schlaubi.lavakord.plugins.sponsorblock.rest.putSponsorblockCategories
import dev.schlaubi.lavakord.rest.loadItem
import kotlinx.serialization.json.JsonNull.content

@OptIn(KordVoice::class)
suspend fun main() {
    val token = getEnvOrThrow("BOT_TOKEN")
    val clientId = getEnvOrThrow("BOT_CLIENT_ID")

    val kord = Kord(token)
    val lavalink =
        kord.lavakord {
            plugins {
                install(Sponsorblock)
            }
        }

    lavalink.addNode(
        serverUri = "ws://localhost:2333",
        password = "youshallnotpass",
    )

    println("Add the bot to your server:")
    println(createAuthUrl(clientId))
    println()

    val playCommand =
        kord.createGlobalChatInputCommand(
            name = "play",
            description = "play some music",
        ) {
            string(
                name = "query",
                description = "The track to be played",
            ) {
                required = true
            }
        }

    val stopCommand =
        kord.createGlobalChatInputCommand(
            name = "stop",
            description = "Stop playing the current track",
        )

    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        println("Received ${interaction.invokedCommandName} command from ${interaction.user.username} on ${interaction.guild.id}")

        val guild = interaction.guild
        val voiceChannelId = interaction.user.getVoiceStateOrNull()?.channelId
        val response = interaction.deferPublicResponse()

        if (voiceChannelId == null) {
            println("User ${interaction.user} is currently not in a voice channel")
            return@on
        }

        guild.activeThreads.collect {
            println("$it")
        }

        val link = guild.getLink(lavalink)
        val player = link.player

        link.node.putSponsorblockCategories(
            guild = guild.id.value,
            categories = listOf(Category.MusicOfftopic),
        )

        player.on<TrackEndEvent> {
            link.disconnectAudio()
        }

        when (interaction.invokedCommandId) {
            playCommand.id -> {
                val trackName = interaction.command.strings["query"]

                if (trackName.isNullOrBlank()) {
                    println("Track name is not provided")
                    return@on
                }

                response.respond {
                    content = "Searching for \"$trackName\"…"
                }

                val search: String =
                    if (trackName.startsWith("http")) {
                        trackName
                    } else {
                        "ytsearch:$trackName"
                    }

                val track: Result<Track> =
                    when (val item = link.loadItem(search)) {
                        is LoadResult.TrackLoaded -> Result.success(item.data)
                        is LoadResult.PlaylistLoaded -> Result.success(item.data.tracks.first())
                        is LoadResult.SearchResult -> Result.success(item.data.tracks.first())
                        is LoadResult.NoMatches -> Result.failure(TrackNotFoundException())
                        is LoadResult.LoadFailed -> Result.failure(Exception(item.data.message))
                    }

                response.respond {
                    track
                        .onSuccess { track ->
                            link.connectAudio(voiceChannelId = voiceChannelId.value)
                            player.playTrack(track)

                            content = "Now Playing"

                            embed {
                                title = track.info.title
                                description = track.info.author
                                image = track.info.artworkUrl
                            }
                        }.onFailure { e ->
                            content =
                                when (e) {
                                    is TrackNotFoundException -> "No results found for $trackName."
                                    else -> "Something wrong happened: ${e.message}"
                                }
                        }
                }
            }

            stopCommand.id -> {
                player.stopTrack()
                link.disconnectAudio()

                response.respond {
                    content = "Playback stopped"
                }
            }

            else -> {
                println("Received interaction for unknown command ${interaction.command}")
            }
        }
    }

    println("Bot is ready and listening")

    kord.login()
}

private class TrackNotFoundException : Exception()

fun createAuthUrl(clientId: String): String {
    val permissions = "2150632448"
    return "https://discord.com/oauth2/authorize?client_id=$clientId&permissions=$permissions&integration_type=0&scope=bot"
}

private fun getEnvOrThrow(key: String): String = System.getenv(key) ?: error("Environment variable not set: $key")
