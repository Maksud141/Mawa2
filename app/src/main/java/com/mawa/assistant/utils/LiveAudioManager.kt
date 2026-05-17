package com.mawa.assistant.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

class LiveAudioManager(private val context: Context) {
    private var audioTrack: AudioTrack? = null

    init {
        try {
            val bufferSize = AudioTrack.getMinBufferSize(24000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            audioTrack = AudioTrack(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build(),
                AudioFormat.Builder().setSampleRate(24000).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),
                bufferSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            audioTrack?.play()
        } catch (e: Exception) {}
    }

    fun playChunk(data: ByteArray) {
        try { audioTrack?.write(data, 0, data.size) } catch (e: Exception) {}
    }

    fun stop() {
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (e: Exception) {}
    }
}
