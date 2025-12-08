package fr.outadoc.bruitage.app

/**
 * An Audio Data Format for OPUS.
 */
class OpusAudioDataFormat(channelCount: Int, sampleRate: Int, chunkSampleCount: Int) {
    val maximumChunkSize: Int = 32 + 1536 * chunkSampleCount / 960
    val expectedChunkSize: Int = 32 + 512 * chunkSampleCount / 960
}
