package fr.outadoc.bruitage.app

interface Strings {
    fun searching(trackName: String): String
    fun notInVoiceChannel(): String
    fun promptListeningTo(trackName: String, artist: String): String
    fun systemPrompt(): String
    fun trackNotFound(trackName: String): String
    fun unknownError(message: String?): String
    fun playBackStopped(): String
    fun commandPlayDescription(): String
    fun commandPlayQueryDescription(): String
    fun commandStopDescription(): String
    fun commandQueueDescription(): String
    fun commandSkipDescription(): String
    fun addedToQueue(trackName: String, position: Int): String
    fun skipped(trackName: String, artist: String): String
    fun queueEmpty(): String
    fun queueList(nowPlaying: String?, upcoming: List<String>): String
}
