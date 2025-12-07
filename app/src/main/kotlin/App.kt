package fr.outadoc.bruitage.app

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val token = checkNotNull(System.getenv("BOT_TOKEN"))
    val clientId = checkNotNull(System.getenv("BOT_CLIENT_ID"))

    val kord = Kord(token)

    println("Add the bot to your server:")
    println(createAuthUrl(clientId))
    println()

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

    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        println("Received: $interaction")

        val response = interaction.deferEphemeralResponse()
        val trackName = interaction.command.strings["track_name"] ?: return@on

        response.respond {
            content = "Will play: $trackName"
        }

        delay(3.seconds)

        response.respond {
            content = "Playing $trackName!"
        }
    }

    println("Bot is ready and listening")

    kord.login()
}

fun download(url: String) {
    // ffmpeg --extract-audio --audio-format opus --sponsorblock-remove music_offtopic
}

fun createAuthUrl(clientId: String): String {
    val permissions = "2150632448"
    return "https://discord.com/oauth2/authorize?client_id=$clientId&permissions=$permissions&integration_type=0&scope=bot"
}
