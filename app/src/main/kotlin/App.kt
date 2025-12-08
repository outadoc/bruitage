package fr.outadoc.bruitage.app

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.kord.common.annotation.KordVoice
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.lavakord.plugins.lavasearch.LavaSearch
import dev.schlaubi.lavakord.plugins.lavasrc.LavaSrc
import dev.schlaubi.lavakord.plugins.sponsorblock.Sponsorblock
import dev.schlaubi.lavakord.plugins.sponsorblock.model.Category
import dev.schlaubi.lavakord.plugins.sponsorblock.rest.putSponsorblockCategories
import dev.schlaubi.lavakord.rest.loadItem

@OptIn(KordVoice::class)
suspend fun main() {
    val token = checkNotNull(System.getenv("BOT_TOKEN"))
    val clientId = checkNotNull(System.getenv("BOT_CLIENT_ID"))

    val kord = Kord(token)
    val lavalink =
        kord.lavakord {
            plugins {
                install(LavaSrc)
                install(LavaSearch)
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
                name = "track_name",
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
        println("Received: $interaction")

        val guild = interaction.guild
        val channel = interaction.user.getVoiceState().channelId
        val response = interaction.deferEphemeralResponse()

        if (channel == null) {
            println("User ${interaction.user} is currently not in a voice channel")
            return@on
        }

        val link = guild.getLink(lavalink)
        val player = link.player

        link.node.putSponsorblockCategories(
            guild = guild.id.value,
            categories = listOf(Category.MusicOfftopic),
        )

        when (interaction.invokedCommandId) {
            playCommand.id -> {
                val trackName = interaction.command.strings["track_name"]

                if (trackName.isNullOrBlank()) {
                    println("Track name is not provided")
                    return@on
                }

                response.respond {
                    content = "Will play: $trackName"
                }

                link.connectAudio(channel.value)

                val search: String =
                    if (trackName.startsWith("http")) {
                        trackName
                    } else {
                        "ytsearch:$trackName"
                    }

                when (val item = link.loadItem(search)) {
                    is LoadResult.TrackLoaded -> {
                        response.respond {
                            content = "Playing track ${item.data.info.title}"
                        }

                        player.playTrack(track = item.data)
                    }

                    is LoadResult.PlaylistLoaded -> {
                        val track = item.data.tracks.first()
                        response.respond {
                            content = "Playing playlist ${track.info.title}"
                        }

                        player.playTrack(track)
                    }

                    is LoadResult.SearchResult -> {
                        val track = item.data.tracks.first()
                        response.respond {
                            content = "Playing track ${track.info.title}"
                        }

                        player.playTrack(track)
                    }

                    is LoadResult.NoMatches -> {
                        response.respond { content = "No matches" }
                    }

                    is LoadResult.LoadFailed -> {
                        response.respond { content = item.data.message ?: "Exception" }
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

private val DISCORD_OPUS =
    OpusAudioDataFormat(
        channelCount = 2,
        sampleRate = 48000,
        chunkSampleCount = 960,
    )

fun download(url: String) {
    // ffmpeg --extract-audio --audio-format opus --sponsorblock-remove music_offtopic --no-part -o out.opus
}

fun createAuthUrl(clientId: String): String {
    val permissions = "2150632448"
    return "https://discord.com/oauth2/authorize?client_id=$clientId&permissions=$permissions&integration_type=0&scope=bot"
}
