package com.togglestudios.lumenpop.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentMusicTrack: String? = null
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                stopMusic()
            } else if (currentMusicTrack != null) {
                playMusic(currentMusicTrack!!)
            }
        }

    var isSoundEnabled: Boolean = true

    fun playMusic(assetName: String) {
        currentMusicTrack = assetName
        if (!isMusicEnabled) return

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            val afd = context.assets.openFd(assetName)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = true
                setVolume(0.4f, 0.4f)
                prepare()
                start()
            }
            afd.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopMusic() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSound(kind: String, intensity: Int = 1) {
        if (!isSoundEnabled) return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (kind) {
                    "wake" -> playTone(420.0, 120, 0.08f)
                    "link" -> playTone(510.0 + (intensity * 35.0), 90, 0.08f)
                    "backtrack" -> playTone(260.0, 100, 0.06f)
                    "pop" -> {
                        playTone(560.0 + (intensity * 35.0), 120, 0.12f)
                        playTone(840.0 + (intensity * 55.0), 140, 0.09f)
                    }
                    "special" -> {
                        playTone(220.0, 200, 0.15f)
                        playTone(660.0, 250, 0.12f)
                        playTone(990.0, 300, 0.10f)
                    }
                    "booster" -> {
                        playTone(280.0, 150, 0.12f)
                        playTone(620.0, 200, 0.10f)
                    }
                    "win" -> {
                        playTone(520.0, 180, 0.12f)
                        playTone(660.0, 200, 0.12f)
                        playTone(880.0, 320, 0.15f)
                    }
                    "lose" -> {
                        playTone(300.0, 200, 0.10f)
                        playTone(220.0, 300, 0.10f)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun playTone(frequencyHz: Double, durationMs: Int, volume: Float) {
        val sampleRate = 44100
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            val angle = 2.0 * Math.PI * frequencyHz * time
            val envelope = when {
                i < numSamples * 0.1 -> (i / (numSamples * 0.1)).toFloat()
                i > numSamples * 0.7 -> ((numSamples - i) / (numSamples * 0.3)).toFloat()
                else -> 1.0f
            }
            val sample = (sin(angle) * Short.MAX_VALUE * volume * envelope).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()
    }

    fun release() {
        stopMusic()
    }
}
