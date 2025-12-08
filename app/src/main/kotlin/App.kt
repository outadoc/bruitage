package fr.outadoc.bruitage.app

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection

@OptIn(KordVoice::class)
suspend fun main() {
    val token = checkNotNull(System.getenv("BOT_TOKEN"))
    val clientId = checkNotNull(System.getenv("BOT_CLIENT_ID"))

    val kord = Kord(token)

    println("Add the bot to your server:")
    println(createAuthUrl(clientId))
    println()

    val playCommand = kord.createGlobalChatInputCommand(
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

    val stopCommand = kord.createGlobalChatInputCommand(
        name = "stop",
        description = "Stop playing the current track"
    )

    // here we keep track of active voice connections
    val connections: MutableMap<Snowflake, VoiceConnection> = mutableMapOf()

    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        println("Received: $interaction")

        val guildId = interaction.guildId
        val channel = interaction.user.getVoiceState().getChannelOrNull()

        if (channel == null) {
            println("User ${interaction.user} is currently not in a voice channel")
            return@on
        }

        when (interaction.invokedCommandId) {
            playCommand.id -> {
                val trackName = interaction.command.strings["track_name"]

                if (trackName.isNullOrBlank()) {
                    println("Track name is not provided")
                }

                val response = interaction.deferEphemeralResponse()

                response.respond {
                    content = "Will play: $trackName"
                }

                // Let's close the old connection if there is one
                connections.remove(guildId)?.shutdown()

                val connection =
                    channel.connect {
                        selfDeaf = true
                        audioProvider {
                            //AudioFrame.fromData(player.provide()?.data)
                            AudioFrame.SILENCE
                        }
                    }

                connections[guildId] = connection

                response.respond {
                    content = "Playing $trackName!"
                }
            }

            stopCommand.id -> {
                val response = interaction.deferEphemeralResponse()

                connections.remove(guildId)?.shutdown()

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

fun download(url: String) {
    // ffmpeg --extract-audio --audio-format opus --sponsorblock-remove music_offtopic --no-part -o out.opus
}

fun createAuthUrl(clientId: String): String {
    val permissions = "2150632448"
    return "https://discord.com/oauth2/authorize?client_id=$clientId&permissions=$permissions&integration_type=0&scope=bot"
}
