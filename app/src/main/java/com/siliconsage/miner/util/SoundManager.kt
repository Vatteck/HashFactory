package com.siliconsage.miner.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

object SoundManager {
    private var soundPool: SoundPool? = null
    private val soundMap = ConcurrentHashMap<String, Int>()
    
    private const val PREFS_NAME = "audio_prefs"
    private const val KEY_SFX_ENABLED = "sfx_enabled"
    private const val KEY_SFX_VOLUME = "sfx_volume"
    private const val KEY_BGM_ENABLED = "bgm_enabled"
    private const val KEY_BGM_VOLUME = "bgm_volume"
    private const val KEY_CUSTOM_URI = "custom_bgm_uri"
    private const val KEY_SELECTED_BGM_TRACK = "selected_bgm_track"
    private const val PREFS_SFX_OVERRIDES = "sfx_overrides"
    
    // --- Independent Controls ---
    val sfxVolume = MutableStateFlow(0.5f)
    val isSfxEnabled = MutableStateFlow(true)
    val bgmVolume = MutableStateFlow(0.8f)
    val isBgmEnabled = MutableStateFlow(true)
    val selectedBgmTrack = MutableStateFlow("bgm.ogg")
    
    private val customSfxMap = ConcurrentHashMap<String, String>() // name -> uri
    private val customSfxPlayers = ConcurrentHashMap<String, MediaPlayer>()

    fun setSfxVolume(value: Float) {
        sfxVolume.value = value
        saveSetting(KEY_SFX_VOLUME, value)
        updateActiveSfxVolume()
    }
    
    fun setSfxEnabled(enabled: Boolean) {
        isSfxEnabled.value = enabled
        saveSetting(KEY_SFX_ENABLED, enabled)
        if (!enabled) {
            soundPool?.autoPause()
        } else {
            soundPool?.autoResume()
        }
    }
    
    fun setBgmVolume(value: Float) {
        bgmVolume.value = value
        saveSetting(KEY_BGM_VOLUME, value)
        updateBgmVolume()
    }
    
    fun setBgmEnabled(enabled: Boolean) {
        isBgmEnabled.value = enabled
        saveSetting(KEY_BGM_ENABLED, enabled)
        if (!enabled) {
            stopBgm()
        } else {
            startBgm()
        }
    }

    // --- Background Music ---
    private var bgmPlayer: MediaPlayer? = null
    private var bgmJob: kotlinx.coroutines.Job? = null
    private var isBgmPlaying = false
    private var bgmStage = 0 
    private var appCtx: Context? = null
    
    // Custom Music Support
    var customMusicUri: String? = null 

    fun setSelectedBgmTrack(track: String) {
        selectedBgmTrack.value = track
        saveSetting(KEY_SELECTED_BGM_TRACK, track)
        startBgm()
    }

    fun init(ctx: Context) {
        if (appCtx != null) return // Already initialized

        try {
            appCtx = ctx.applicationContext
            
            // Load Preferences
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Update state flows
            isSfxEnabled.value = prefs.getBoolean(KEY_SFX_ENABLED, true)
            sfxVolume.value = prefs.getFloat(KEY_SFX_VOLUME, 0.5f)
            isBgmEnabled.value = prefs.getBoolean(KEY_BGM_ENABLED, true)
            bgmVolume.value = prefs.getFloat(KEY_BGM_VOLUME, 0.8f)
            customMusicUri = prefs.getString(KEY_CUSTOM_URI, null)
            selectedBgmTrack.value = prefs.getString(KEY_SELECTED_BGM_TRACK, "bgm.ogg") ?: "bgm.ogg"
            
            // Load SFX Overrides
            val sfxPrefs = ctx.getSharedPreferences(PREFS_SFX_OVERRIDES, Context.MODE_PRIVATE)
            sfxPrefs.all.forEach { (key, value) ->
                if (value is String) customSfxMap[key] = value
            }
            
            // SFX Pool
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
                
            soundPool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attributes)
                .build()
                
            loadSounds()
            
            // Start BGM if enabled
            if (isBgmEnabled.value) startBgm()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateActiveSfxVolume() {
        val pool = soundPool ?: return
        val vol = sfxVolume.value
        if (humStreamId != 0) pool.setVolume(humStreamId, vol, vol)
        if (alarmStreamId != 0) pool.setVolume(alarmStreamId, vol, vol)
        if (thrumStreamId != 0) pool.setVolume(thrumStreamId, vol, vol)
    }

    private fun loadSounds() {
        val ctx = appCtx ?: return
        CoroutineScope(Dispatchers.IO).launch {
            // === CORE UI ===
            
            // 1. Click — Crisp tick with subtle overtone
            val clickBase = AudioGenerator.generateTone(2500.0, 10, AudioGenerator.WaveType.SINE, 0.08)
            val clickOvertone = AudioGenerator.generateTone(5000.0, 8, AudioGenerator.WaveType.SINE, 0.03)
            loadPcm(ctx, "click", clickBase + clickOvertone)

            // 2. Buy — Layered harmonic chord (C5+E5+G5)
            val buyPcm = AudioGenerator.generateChord(listOf(523.25, 659.25, 783.99), 80, 0.12)
            loadPcm(ctx, "buy", buyPcm)

            // 3. Error — Low buzz with square-wave menace
            val errSine = AudioGenerator.generateTone(180.0, 150, AudioGenerator.WaveType.SINE, 0.15)
            val errBuzz = AudioGenerator.generateTone(90.0, 150, AudioGenerator.WaveType.SQUARE, 0.06)
            loadPcm(ctx, "error", errSine + errBuzz)
            
            // 4. Glitch — Bit-crushed noise + random sine stab
            val glitchNoise = AudioGenerator.generateTone(0.0, 60, AudioGenerator.WaveType.NOISE, 0.06)
            val glitchStab = AudioGenerator.generateTone(1800.0, 30, AudioGenerator.WaveType.SQUARE, 0.04)
            loadPcm(ctx, "glitch", glitchNoise + glitchStab)
            
            // === MARKET ===
            
            // 5. Market UP — Rising slide with harmonic tail
            val marketUpSlide = AudioGenerator.generateSlide(600.0, 1400.0, 200, 0.08)
            val marketUpChime = AudioGenerator.generateChord(listOf(1400.0, 1750.0), 60, 0.05)
            loadPcm(ctx, "market_up", marketUpSlide + marketUpChime)
            
            // 6. Market DOWN — Descending slide with low thud
            val marketDownSlide = AudioGenerator.generateSlide(600.0, 250.0, 300, 0.08)
            val marketDownThud = AudioGenerator.generateTone(120.0, 80, AudioGenerator.WaveType.SINE, 0.06)
            loadPcm(ctx, "market_down", marketDownSlide + marketDownThud)

            // === ALERTS & AMBIENT ===

            // 7. Alarm — Fast alternating warble (urgent siren)
            val alarmHigh = AudioGenerator.generateTone(2200.0, 60, AudioGenerator.WaveType.SINE, 0.09)
            val alarmLow = AudioGenerator.generateTone(1600.0, 60, AudioGenerator.WaveType.SINE, 0.09)
            loadPcm(ctx, "alarm", alarmHigh + alarmLow + alarmHigh + alarmLow)

            // 8. Hum — Steady low drone
            val humPcm = AudioGenerator.generateTone(150.0, 500, AudioGenerator.WaveType.SINE, 0.01) 
            loadPcm(ctx, "hum", humPcm)
            
            // 9. Type — Tiny glass ping for news ticker
            val typePcm = AudioGenerator.generateTone(3500.0, 8, AudioGenerator.WaveType.SINE, 0.02)
            loadPcm(ctx, "type", typePcm)
            
            // 10. Thrum — Steady dark hum @ 85Hz with triangle warmth
            val thrumPcm = AudioGenerator.generateTone(85.0, 1000, AudioGenerator.WaveType.TRIANGLE, 0.1, isLoop = true)
            loadPcm(ctx, "thrum", thrumPcm)
            
            // 11. Steam — Filtered noise burst (cooling purge)
            val steamPcm = AudioGenerator.generateTone(0.0, 600, AudioGenerator.WaveType.NOISE, 0.1)
            loadPcm(ctx, "steam", steamPcm)
            
            // 12. Scream Synth — Bit-crushed descending noise for Void
            val screamPcm = AudioGenerator.generateSlide(3000.0, 100.0, 800, 0.5)
            loadPcm(ctx, "scream_synth", screamPcm)
            
            // 13. Message Received — Crystal-clear ascending chirp
            val msg1 = AudioGenerator.generateTone(1200.0, 40, AudioGenerator.WaveType.SINE, 0.1)
            val msg2 = AudioGenerator.generateTone(1800.0, 40, AudioGenerator.WaveType.SINE, 0.1)
            val msg3 = AudioGenerator.generateTone(2400.0, 80, AudioGenerator.WaveType.SINE, 0.08)
            loadPcm(ctx, "message_received", msg1 + msg2 + msg3)
            
            // === PREVIOUSLY MISSING (Ghost Sounds) ===
            
            // 14. Startup — Warm rising chord (C4→G4→C5), system boot
            val startupPcm = AudioGenerator.generateChord(listOf(261.63, 392.00, 523.25), 250, 0.12)
            loadPcm(ctx, "startup", startupPcm)
            
            // 15. Success — Bright ascending triad (E5→G#5→B5), tech tree unlock
            val successPcm = AudioGenerator.generateChord(listOf(659.25, 830.61, 987.77), 180, 0.10)
            loadPcm(ctx, "success", successPcm)
            
            // 16. Victory — Triumphant 4-note arpeggio (C4→E4→G4→C5), sector capture
            val vic1 = AudioGenerator.generateTone(261.63, 80, AudioGenerator.WaveType.SINE, 0.10)
            val vic2 = AudioGenerator.generateTone(329.63, 80, AudioGenerator.WaveType.SINE, 0.10)
            val vic3 = AudioGenerator.generateTone(392.00, 80, AudioGenerator.WaveType.SINE, 0.10)
            val vic4 = AudioGenerator.generateChord(listOf(523.25, 659.25, 783.99), 200, 0.12)
            loadPcm(ctx, "victory", vic1 + vic2 + vic3 + vic4)
            
            // 17. Alert — Urgent double-pulse square wave (narrative warning)
            val alertPulse = AudioGenerator.generateTone(1200.0, 60, AudioGenerator.WaveType.SQUARE, 0.08)
            val alertGap = ByteArray((0.04 * AudioGenerator.SAMPLE_RATE * 2).toInt()) // 40ms silence
            loadPcm(ctx, "alert", alertPulse + alertGap + alertPulse)
            
            // 18. Data Recovered — FM chirp: descending slide + rising tail
            val dataDown = AudioGenerator.generateSlide(3000.0, 1500.0, 120, 0.08)
            val dataUp = AudioGenerator.generateSlide(1500.0, 2500.0, 100, 0.06)
            loadPcm(ctx, "data_recovered", dataDown + dataUp)
            
            // 19. Climax Impact — Heavy: noise burst + deep rumble (narrative climax)
            val climaxPcm = AudioGenerator.generateImpact(noiseMs = 50, tailFreq = 60.0, tailMs = 400, volume = 0.5)
            loadPcm(ctx, "climax_impact", climaxPcm)
        }
    }
    
    private fun loadPcm(ctx: Context, name: String, pcmData: ByteArray) {
        try {
            val file = File(ctx.cacheDir, "${name}.wav")
            val wavData = addWavHeader(pcmData)
            file.writeBytes(wavData)
            val soundId = soundPool?.load(file.absolutePath, 1) ?: -1
            soundMap[name] = soundId
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Looping SFX
    private var humStreamId = 0
    private var alarmStreamId = 0
    private var thrumStreamId = 0

    private var isAppPaused = false

    fun play(soundName: String, pan: Float = 0f, loop: Boolean = false, pitch: Float = 1f) {
        if (!isSfxEnabled.value || isAppPaused) return
        
        // v3.4.64: Check for Custom SFX Override first
        val customUri = customSfxMap[soundName]
        if (customUri != null) {
            playCustomSfx(soundName, customUri, pan, loop, pitch)
            return
        }

        val soundId = soundMap[soundName] ?: return
        
        val leftVol = sfxVolume.value * (if (pan > 0) 1f - pan else 1f)
        val rightVol = sfxVolume.value * (if (pan < 0) 1f + pan else 1f)
        
        // Pitch range is 0.5 to 2.0
        val safePitch = pitch.coerceIn(0.5f, 2.0f)
        
        val streamId = soundPool?.play(soundId, leftVol, rightVol, 1, if(loop) -1 else 0, safePitch) ?: 0
        
        if (soundName == "hum") humStreamId = streamId
        if (soundName == "alarm") alarmStreamId = streamId
        if (soundName == "thrum") thrumStreamId = streamId
    }

    private fun playCustomSfx(name: String, uriStr: String, pan: Float, loop: Boolean, pitch: Float) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val uri = uriStr.toUri()
                val player = MediaPlayer()
                appCtx?.let { ctx ->
                    player.setDataSource(ctx, uri)
                    player.isLooping = loop
                    player.prepare()
                    
                    val vol = sfxVolume.value
                    val leftVol = vol * (if (pan > 0) 1f - pan else 1f)
                    val rightVol = vol * (if (pan < 0) 1f + pan else 1f)
                    player.setVolume(leftVol, rightVol)
                    
                    // Note: MediaPlayer doesn't support easy pitch shifting without API 23+ (PlaybackParams)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        player.playbackParams = player.playbackParams.setSpeed(pitch)
                    }

                    player.setOnCompletionListener { it.release() }
                    player.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to default if custom fails
                customSfxMap.remove(name)
            }
        }
    }

    fun setCustomSfx(name: String, uri: String?) {
        if (uri == null) {
            customSfxMap.remove(name)
            appCtx?.getSharedPreferences(PREFS_SFX_OVERRIDES, Context.MODE_PRIVATE)?.edit {
                remove(name)
            }
        } else {
            customSfxMap[name] = uri
            appCtx?.getSharedPreferences(PREFS_SFX_OVERRIDES, Context.MODE_PRIVATE)?.edit {
                putString(name, uri)
            }
        }
    }

    fun clearAllOverrides() {
        customMusicUri = null
        customSfxMap.clear()
        appCtx?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit { remove(KEY_CUSTOM_URI) }
        appCtx?.getSharedPreferences(PREFS_SFX_OVERRIDES, Context.MODE_PRIVATE)?.edit { clear() }
        startBgm()
    }

    // Helper to update pitch of ongoing loop (SoundPool doesn't support changing rate of active stream easily pre-API 23, but we can try setRate)
    fun setLoopPitch(soundName: String, pitch: Float) {
        val streamId = when(soundName) {
            "hum" -> humStreamId
            "thrum" -> thrumStreamId
            else -> 0
        }
        if (streamId != 0 && soundPool != null) {
            val safePitch = pitch.coerceIn(0.5f, 2.0f)
            soundPool?.setRate(streamId, safePitch)
        }
    }

    fun stop(soundName: String) {
        when(soundName) {
            "hum" -> { soundPool?.stop(humStreamId); humStreamId = 0 }
            "alarm" -> { soundPool?.stop(alarmStreamId); alarmStreamId = 0 }
            "thrum" -> { soundPool?.stop(thrumStreamId); thrumStreamId = 0 }
        }
    }

    // --- BGM Logic ---

    private var staticAudioTrack: AudioTrack? = null
    
    fun setBgmStage(stage: Int) {
        bgmStage = stage
    }
    
    fun setCustomTrack(uri: String?) {
        customMusicUri = uri
        appCtx?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit {
            putString(KEY_CUSTOM_URI, uri)
        }
        startBgm()
    }

    private fun startBgm() {
        if (!isBgmEnabled.value) return
        stopBgm()
        bgmJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                if (customMusicUri != null) {
                    playCustomBgm()
                } else {
                    playAssetBgm()
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun playAssetBgm() {
        val ctx = appCtx ?: return
        withContext(Dispatchers.Main) {
            try {
                val player = MediaPlayer()
                val descriptor = ctx.assets.openFd(selectedBgmTrack.value)
                player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                player.isLooping = true
                player.prepare()
                descriptor.close()
                setupPlayer(player)
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                    playProceduralBgm()
                }
            }
        }
    }

    private suspend fun playCustomBgm() {
        val uriStr = customMusicUri ?: return
        withContext(Dispatchers.Main) {
            try {
                val uri = uriStr.toUri()
                val player = MediaPlayer()
                appCtx?.let { ctx ->
                    player.setDataSource(ctx, uri)
                    player.isLooping = true
                    player.prepare()
                    setupPlayer(player)
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                    customMusicUri = null
                    startBgm()
                }
            }
        }
    }

    private fun playProceduralBgm() {
        try {
            val pcmData = generateBgmTrack(bgmStage)
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_OUT_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = pcmData.size.coerceAtLeast(minBufferSize)
            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build())
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(pcmData, 0, pcmData.size)
            track.setLoopPoints(0, pcmData.size / 2, -1)
            track.setVolume(bgmVolume.value)
            track.play()
            staticAudioTrack = track
            isBgmPlaying = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupPlayer(player: MediaPlayer) {
        bgmPlayer = player
        updateBgmVolume()
        if (isBgmEnabled.value) {
             bgmPlayer?.start()
             isBgmPlaying = true
        } else {
             player.release()
             bgmPlayer = null
        }
    }
    
    private fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
        try {
            bgmPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            bgmPlayer = null
            staticAudioTrack?.let {
                it.stop()
                it.release()
            }
            staticAudioTrack = null
            isBgmPlaying = false
        } catch (e: Exception) { e.printStackTrace() }
    }
    
    private fun updateBgmVolume() {
        try {
            bgmPlayer?.setVolume(bgmVolume.value, bgmVolume.value)
            staticAudioTrack?.setVolume(bgmVolume.value)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun generateBgmTrack(stage: Int): ByteArray {
        val freqBase = 100.0
        val freqHari = 102.0
        val numSamples = 44100 * 4
        val buffer = ByteArray(numSamples * 2)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / 44100.0
            var s = kotlin.math.sin(2.0 * kotlin.math.PI * freqBase * t) * 0.6
            s += kotlin.math.sin(2.0 * kotlin.math.PI * freqHari * t) * 0.4
            s += kotlin.math.sin(2.0 * kotlin.math.PI * (freqBase * 2) * t) * 0.2
            if (stage >= 1) {
                if (i % 22050 < 1000) { s += Random.nextDouble(-0.3, 0.3) }
            }
            if (stage >= 3) {
                 s += kotlin.math.sin(2.0 * kotlin.math.PI * (freqBase * 1.2) * t) * 0.3
            }
            val val16 = (s * 0.8 * 32767).toInt().coerceIn(-32768, 32767).toShort()
            buffer[2 * i] = (val16.toInt() and 0xff).toByte()
            buffer[2 * i + 1] = ((val16.toInt() and 0xff00) ushr 8).toByte()
        }
        return buffer
    }

    private fun addWavHeader(pcmData: ByteArray): ByteArray {
        val header = ByteArray(44)
        val totalDataLen = pcmData.size + 36
        val bitrate = 44100 * 16 * 1 / 8
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0
        header[22] = 1; header[23] = 0
        header[24] = (44100 and 0xff).toByte()
        header[25] = ((44100 shr 8) and 0xff).toByte()
        header[26] = ((44100 shr 16) and 0xff).toByte()
        header[27] = ((44100 shr 24) and 0xff).toByte()
        header[28] = (bitrate and 0xff).toByte()
        header[29] = ((bitrate shr 8) and 0xff).toByte()
        header[30] = ((bitrate shr 16) and 0xff).toByte()
        header[31] = ((bitrate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0
        header[34] = 16; header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (pcmData.size and 0xff).toByte()
        header[41] = ((pcmData.size shr 8) and 0xff).toByte()
        header[42] = ((pcmData.size shr 16) and 0xff).toByte()
        header[43] = ((pcmData.size shr 24) and 0xff).toByte()
        return header + pcmData
    }
    
    fun pauseAll() {
        isAppPaused = true
        soundPool?.autoPause()
        bgmPlayer?.pause()
        staticAudioTrack?.pause()
    }
    
    fun resumeAll() {
        isAppPaused = false
        if (isSfxEnabled.value) soundPool?.autoResume()
        if (isBgmEnabled.value && isBgmPlaying) {
            bgmPlayer?.start()
            staticAudioTrack?.play()
        }
    }

    fun release() {
        stopBgm()
        soundPool?.release()
        soundPool = null
    }

    private fun saveSetting(key: String, value: Any) {
        val prefs = appCtx?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        prefs.edit {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is String -> putString(key, value)
            }
        }
    }

    fun resetSettings(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { clear() }
        setSfxEnabled(true)
        setSfxVolume(0.5f)
        setBgmEnabled(true)
        setBgmVolume(0.8f)
        customMusicUri = null
        if (isBgmEnabled.value) startBgm() else stopBgm()
    }
}
