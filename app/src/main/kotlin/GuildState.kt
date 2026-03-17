package fr.outadoc.bruitage.app

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.lavakord.audio.Link
import java.util.concurrent.LinkedBlockingDeque

class GuildState(
    val link: Link,
) {
    private val queue = LinkedBlockingDeque<Track>()

    fun enqueue(track: Track): Int {
        queue.addLast(track)
        return queue.size - 1
    }

    fun poll(): Track? = queue.pollFirst()

    fun peek(): Track? = queue.peekFirst()

    fun clear() = queue.clear()

    fun snapshot(): List<Track> = queue.toList()
}
