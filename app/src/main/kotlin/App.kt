package fr.outadoc.bruitage.app

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.annotation.KordVoice
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.mistralai.MistralAiChatModel
import dev.langchain4j.model.mistralai.MistralAiChatModelName
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.lavakord.plugins.sponsorblock.Sponsorblock
import dev.schlaubi.lavakord.plugins.sponsorblock.model.Category
import dev.schlaubi.lavakord.plugins.sponsorblock.rest.putSponsorblockCategories
import dev.schlaubi.lavakord.rest.loadItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList

@OptIn(KordVoice::class)
suspend fun main() {
    val token: String = getEnvOrThrow("BOT_TOKEN")
    val clientId: String = getEnvOrThrow("BOT_CLIENT_ID")
    val lavalinkUri: String = getEnvOrThrow("LAVALINK_URI")
    val lavalinkPassword: String = getEnvOrThrow("LAVALINK_PASSWORD")
    val mistralToken: String? = getEnvOrNull("MISTRAL_API_KEY")

    val kord = Kord(token)
    val lavalink =
        kord.lavakord {
            plugins {
                install(Sponsorblock)
            }
        }

    lavalink.addNode(
        serverUri = lavalinkUri,
        password = lavalinkPassword,
    )

    val mistralModel =
        MistralAiChatModel
            .builder()
            .modelName(MistralAiChatModelName.MISTRAL_SMALL_LATEST)
            .apiKey(mistralToken)
            .build()

    println("Add the bot to your server:")
    println(createAuthUrl(clientId))
    println()

    // Clean up old commands
    kord
        .getGlobalApplicationCommands()
        .onEach { it.delete() }
        .collect()

    val playCommand =
        kord.createGlobalChatInputCommand(
            name = "play",
            description = Strings.commandPlayDescription(),
        ) {
            string(
                name = "query",
                description = Strings.commandPlayQueryDescription(),
            ) {
                required = true
            }
        }

    val skipCommand =
        kord.createGlobalChatInputCommand(
            name = "skip",
            description = Strings.commandSkipDescription(),
        )

    val stopCommand =
        kord.createGlobalChatInputCommand(
            name = "stop",
            description = Strings.commandStopDescription(),
        )

    val queueCommand =
        kord.createGlobalChatInputCommand(
            name = "queue",
            description = Strings.commandQueueDescription(),
        )

    /**
     * Plays the next track in the queue for the given guild/link.
     * Disconnects audio if the queue is empty.
     */
    suspend fun playNext(guildId: ULong) {
        val state =
            GuildStateManager.getOrCreate(guildId, lavalink) { id ->
                println("TrackEndEvent")
                playNext(id)
            }

        val next = state.poll()
        if (next == null) {
            state.link.disconnectAudio()
            return
        }

        state.link.player.playTrack(next)
    }

    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        println("Received ${interaction.invokedCommandName} command from ${interaction.user.username} on ${interaction.guild.id}")

        val guild = interaction.guild
        val guildId = guild.id.value
        val voiceChannelId = interaction.user.getVoiceStateOrNull()?.channelId

        if (voiceChannelId == null) {
            println("User ${interaction.user} is currently not in a voice channel")
            return@on
        }

        val state =
            GuildStateManager.getOrCreate(guildId, lavalink) { id ->
                println("TrackEndEvent")
                playNext(id)
            }

        val link = state.link
        val player = link.player

        link.node.putSponsorblockCategories(
            guild = guildId,
            categories = listOf(Category.MusicOfftopic),
        )

        when (interaction.invokedCommandId) {
            // ── /play ──────────────────────────────────────────────────────────
            playCommand.id -> {
                val trackName = interaction.command.strings["query"]

                if (trackName.isNullOrBlank()) {
                    println("Track name is not provided")
                    return@on
                }

                val response =
                    interaction.respondPublic {
                        content = Strings.searching(trackName)
                    }

                val search: String =
                    if (trackName.startsWith("http")) {
                        trackName
                    } else {
                        "ytsearch:$trackName"
                    }

                val trackResult: Result<Track> =
                    when (val item = link.loadItem(search)) {
                        is LoadResult.TrackLoaded -> Result.success(item.data)
                        is LoadResult.PlaylistLoaded -> Result.success(item.data.tracks.first())
                        is LoadResult.SearchResult -> Result.success(item.data.tracks.first())
                        is LoadResult.NoMatches -> Result.failure(TrackNotFoundException())
                        is LoadResult.LoadFailed -> Result.failure(Exception(item.data.message))
                    }

                trackResult
                    .onSuccess { track ->
                        val position = state.enqueue(track)
                        val isFirstTrack = player.playingTrack == null

                        // Only connect + play immediately if this is the first (and only) track.
                        if (isFirstTrack) {
                            link.connectAudio(voiceChannelId = voiceChannelId.value)

                            // Pop it back out of the queue and play it directly.
                            playNext(guildId)
                        }

                        val chatRequest =
                            ChatRequest
                                .builder()
                                .messages(
                                    SystemMessage(Strings.systemPrompt()),
                                    UserMessage(
                                        Strings.promptListeningTo(
                                            trackName = track.info.title,
                                            artist = track.info.author,
                                        ),
                                    ),
                                ).build()

                        response.createPublicFollowup {
                            embed {
                                title = track.info.title
                                description = track.info.author
                                image = track.info.artworkUrl
                            }

                            content =
                                buildString {
                                    appendLine(Strings.addedToQueue(track.info.title, position))
                                    appendLine()

                                    try {
                                        appendLine(
                                            mistralModel.chat(chatRequest).aiMessage().text(),
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                        }
                    }.onFailure { e ->
                        response.createPublicFollowup {
                            content =
                                when (e) {
                                    is TrackNotFoundException -> Strings.trackNotFound(trackName)
                                    else -> Strings.unknownError(e.message)
                                }
                        }
                    }
            }

            // ── /skip ──────────────────────────────────────────────────────────
            skipCommand.id -> {
                val upcoming = state.snapshot()

                player.stopTrack()

                interaction.respondPublic {
                    content =
                        if (upcoming.isEmpty()) {
                            // Nothing left after current track
                            Strings.queueEmpty()
                        } else {
                            val next = upcoming.first()
                            Strings.skipped(
                                trackName = next.info.title,
                                artist = next.info.author,
                            )
                        }
                }
            }

            // ── /queue ─────────────────────────────────────────────────────────
            queueCommand.id -> {
                interaction.respondPublic {
                    content =
                        Strings.queueList(
                            nowPlaying = player.playingTrack?.info?.title,
                            upcoming = state.snapshot().map { it.info.title },
                        )
                }
            }

            // ── /stop ──────────────────────────────────────────────────────────
            stopCommand.id -> {
                state.clear()

                player.stopTrack()

                interaction.respondPublic {
                    content = Strings.playBackStopped()
                }
            }

            else -> {
                println("Received interaction for unknown command ${interaction.command}")
            }
        }
    }

    kord.on<VoiceStateUpdateEvent> {
        val guildId = state.guildId.value
        val leftChannelId = old?.channelId ?: return@on

        val guildState = GuildStateManager.get(guildId) ?: return@on
        if (guildState.link.state != Link.State.CONNECTED) {
            return@on
        }

        val guild = state.getGuildOrNull() ?: return@on
        val botChannelId =
            guild
                .getMemberOrNull(kord.selfId)
                ?.getVoiceStateOrNull()
                ?.channelId

        if (leftChannelId != botChannelId) {
            return@on
        }

        val remainingStates =
            guild.voiceStates
                .filter { it.channelId == botChannelId && it.userId != kord.selfId }
                .toList()

        var nonBotRemaining = 0
        for (vs in remainingStates) {
            if (guild.getMemberOrNull(vs.userId)?.isBot != true) {
                nonBotRemaining++
            }
        }

        if (nonBotRemaining == 0) {
            println("Voice channel empty, disconnecting from guild $guildId")
            guildState.clear()
            guildState.link.disconnectAudio()
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

private fun getEnvOrNull(key: String): String? = System.getenv(key)
