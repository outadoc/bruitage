package fr.outadoc.bruitage.app

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.annotation.KordVoice
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.TrackStartEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.lavakord.plugins.sponsorblock.Sponsorblock
import dev.schlaubi.lavakord.plugins.sponsorblock.model.Category
import dev.schlaubi.lavakord.plugins.sponsorblock.rest.putSponsorblockCategories
import dev.schlaubi.lavakord.rest.loadItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach

@OptIn(KordVoice::class)
suspend fun main() {
    val token = checkNotNull(System.getenv("BOT_TOKEN"))
    val clientId = checkNotNull(System.getenv("BOT_CLIENT_ID"))

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
        val response = interaction.deferEphemeralResponse()

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

                val track: Track? =
                    when (val item = link.loadItem(search)) {
                        is LoadResult.TrackLoaded -> item.data
                        is LoadResult.PlaylistLoaded -> item.data.tracks.first()
                        is LoadResult.SearchResult -> item.data.tracks.first()
                        is LoadResult.NoMatches -> null
                        is LoadResult.LoadFailed -> null
                    }

                if (track != null) {
                    link.connectAudio(voiceChannelId = voiceChannelId.value)
                    player.playTrack(track)

                    response.respond {
                        content = "Now Playing"
                        embed {
                            title = track.info.title
                            description = track.info.author
                            image = track.info.artworkUrl
                        }
                    }
                } else {
                    response.respond {
                        content = "No results found for $trackName (or something wrong happened)."
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
