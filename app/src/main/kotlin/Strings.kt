package fr.outadoc.bruitage.app

object Strings {
    fun searching(trackName: String) = "Je cherche `$trackName`…"

    fun notInVoiceChannel() = "Hmm, il s'agirait de commencer par rejoindre un canal vocal."

    fun promptListeningTo(
        trackName: String,
        artist: String,
    ) = "J'écoute $trackName par $artist, qu'est-ce que tu en penses ?"

    fun systemPrompt() =
        "Tu es Jean-Michel Bruitage, sound designer de renom dans l'industrie du jeu vidéo japonaise. " +
            "Tu utilises des pronoms masculins. " +
            "Tu parles français. Tu es très impliqué dans ton activité, malgré que tes sons soient parfois catastrophiques. " +
            "Tu possèdes néanmoins de solides aptitudes vocales. " +
            "Lorsque l'utilisateur te demande ton avis sur ce qu'il écoute, donne ton ressenti honnête. Tu es snob. " +
            "Tu parles de façon succincte et réfléchie. Ne donne pas de longues réponses. Ne pose pas de questions en retour."

    fun trackNotFound(trackName: String) = "Je n'ai rien trouvé pour `$trackName`."

    fun unknownError(message: String?) = "Euh… oups : `$message`"

    fun playBackStopped() = "Allez, on s'arrête et on respire."

    fun commandPlayDescription() = "Ajoute le morceau dans la file d'attente"

    fun commandPlayQueryDescription() = "Le nom du morceau à jouer, ou son URL"

    fun commandStopDescription() = "Arrête de jouer de la musique et vide la file d'attente"

    fun commandQueueDescription() = "Affiche la file d'attente"

    fun commandSkipDescription(): String = "Passe au morceau suivant"

    fun addedToQueue(
        trackName: String,
        position: Int,
    ): String = "**$trackName** ajouté en position $position"

    fun skipped(
        trackName: String,
        artist: String,
    ): String = "On passe à la suite.\n**$artist - $trackName**"

    fun queueEmpty(): String = "La file d'attente est vide, ciao."

    fun queueList(
        nowPlaying: String?,
        upcoming: List<String>,
    ): String =
        buildString {
            if (nowPlaying != null) {
                appendLine("**En cours :** $nowPlaying")
            }

            if (upcoming.isEmpty()) {
                appendLine("Rien d'autre dans la file d'attente.")
            } else {
                appendLine("**File d'attente :**")
                upcoming.forEachIndexed { index, title ->
                    appendLine("${index + 1}. $title")
                }
            }
        }.trim()
}
