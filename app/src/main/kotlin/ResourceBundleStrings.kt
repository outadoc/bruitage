package fr.outadoc.bruitage.app

import java.text.MessageFormat
import java.util.Locale
import java.util.ResourceBundle

class ResourceBundleStrings(locale: Locale = Locale.FRENCH) : Strings {
    private val bundle: ResourceBundle = ResourceBundle.getBundle("messages_fr", locale)

    private fun str(key: String): String = bundle.getString(key)
    private fun fmt(key: String, vararg args: Any?): String = MessageFormat.format(bundle.getString(key), *args)

    override fun searching(trackName: String) = fmt("searching", trackName)
    override fun notInVoiceChannel() = str("notInVoiceChannel")
    override fun promptListeningTo(trackName: String, artist: String) = fmt("promptListeningTo", trackName, artist)
    override fun systemPrompt() = str("systemPrompt")
    override fun trackNotFound(trackName: String) = fmt("trackNotFound", trackName)
    override fun unknownError(message: String?) = fmt("unknownError", message)
    override fun playBackStopped() = str("playBackStopped")
    override fun commandPlayDescription() = str("commandPlayDescription")
    override fun commandPlayQueryDescription() = str("commandPlayQueryDescription")
    override fun commandStopDescription() = str("commandStopDescription")
    override fun commandQueueDescription() = str("commandQueueDescription")
    override fun commandSkipDescription() = str("commandSkipDescription")
    override fun addedToQueue(trackName: String, position: Int) = fmt("addedToQueue", trackName, position)
    override fun skipped(trackName: String, artist: String) = fmt("skipped", trackName, artist)
    override fun queueEmpty() = str("queueEmpty")

    override fun queueList(nowPlaying: String?, upcoming: List<String>): String =
        buildString {
            if (nowPlaying != null) appendLine(fmt("queueList.nowPlaying", nowPlaying))
            if (upcoming.isEmpty()) {
                appendLine(str("queueList.nothingElse"))
            } else {
                appendLine(str("queueList.header"))
                upcoming.forEachIndexed { index, title ->
                    appendLine(fmt("queueList.item", index + 1, title))
                }
            }
        }.trim()
}
