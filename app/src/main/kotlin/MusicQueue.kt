package fr.outadoc.bruitage.app

import dev.arbjerg.lavalink.protocol.v4.Track
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque

/**
 * Manages per-guild music queues.
 */
object MusicQueueManager {
    private val queues = ConcurrentHashMap<ULong, LinkedBlockingDeque<Track>>()

    /** Returns the queue for the given guild, creating it if necessary. */
    private fun queueFor(guildId: ULong): LinkedBlockingDeque<Track> = queues.getOrPut(guildId) { LinkedBlockingDeque() }

    /** Adds a track at the end of the queue. Returns the position (0-based). */
    fun enqueue(
        guildId: ULong,
        track: Track,
    ): Int {
        val queue = queueFor(guildId)
        queue.addLast(track)
        return queue.size - 1
    }

    /**
     * Removes and returns the next track in the queue, or null if empty.
     * This is called both when a track ends naturally and when /skip is used.
     */
    fun poll(guildId: ULong): Track? = queueFor(guildId).pollFirst()

    /** Returns the next track without removing it. */
    fun peek(guildId: ULong): Track? = queueFor(guildId).peekFirst()

    /** Clears the queue for the given guild. */
    fun clear(guildId: ULong) = queueFor(guildId).clear()

    /** Returns a snapshot of the queue (does not include the currently playing track). */
    fun snapshot(guildId: ULong): List<Track> = queueFor(guildId).toList()
}
