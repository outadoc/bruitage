package fr.outadoc.bruitage.app

object Strings {
    fun searching(trackName: String) = "Je cherche `$trackName`…"

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

    fun commandSkipDescription(): String = "Passe au morceau suivant"

    fun addedToQueue(
        title: String,
        position: Int,
    ): String = "**$title** ajouté en position $position"

    fun skipped(nextTitle: String): String = "On passe à la suite."

    fun queueEmpty(): String = "La file d'attente est vide, ciao."
}
