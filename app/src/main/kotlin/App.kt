package fr.outadoc.bruitage.app

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.interaction.string

@OptIn(PrivilegedIntent::class)
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
    }

    kord.on<MessageCreateEvent> {
        // runs every time a message is created that our bot can read

        val author: User =
            message.author ?: return@on

        // ignore other bots, even ourselves. We only serve humans here!
        if (author.isBot) return@on

        when (message.content) {
            "ping" -> {
                message.channel.createMessage("pong!")
            }

            else -> {}
        }
    }

    println("Bot is ready and listening")

    kord.login {
        // we need to specify this to receive the content of messages
        intents += Intent.MessageContent
    }
}

fun createAuthUrl(clientId: String): String {
    val permissions = "2150632448"
    return "https://discord.com/oauth2/authorize?client_id=$clientId&permissions=$permissions&integration_type=0&scope=bot"
}
