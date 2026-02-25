package com.phantomcrowd.data

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * SOS Manager for SafeHer AR.
 *
 * Handles emergency SOS activation:
 * 1. Logs SOS event locally (SharedPreferences)
 * 2. Vibrates the device in SOS pattern (... --- ...)
 * 3. Optional fake-call simulation (plays ringtone)
 * 4. Provides recent SOS log retrieval
 */
object SOSManager {
    private const val TAG = "SOSManager"
    private const val PREFS_NAME = "safeher_sos_logs"
    private const val SOS_LOG_KEY = "sos_events"
    
    // SOS Morse: ··· ─── ···
    private val SOS_VIBRATION_PATTERN = longArrayOf(
        0,    // start immediately
        100, 100, 100, 100, 100, 200, // S: · · ·
        300, 200, 300, 200, 300, 200  // O: ─ ─ ─  (S again implied by repeating)
    )
    
    private var fakeCallPlayer: MediaPlayer? = null
    
    /**
     * Trigger SOS — logs event, vibrates, optionally plays fake call.
     *
     * @param context          Application context
     * @param latitude         Current latitude
     * @param longitude        Current longitude
     * @param enableFakeCall   If true, plays the default ringtone to simulate incoming call
     * @return The logged SOS event
     */
    fun triggerSOS(
        context: Context,
        latitude: Double,
        longitude: Double,
        enableFakeCall: Boolean = false
    ): SOSEvent {
        val event = SOSEvent(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude,
            status = "ACTIVE"
        )
        
        // Log locally
        logSOSEvent(context, event)
        
        // Vibrate SOS pattern
        vibrateSOSPattern(context)
        
        // Fake call simulation
        if (enableFakeCall) {
            startFakeCall(context)
        }
        
        Log.d(TAG, "SOS triggered: $event")
        return event
    }
    
    /**
     * Cancel active SOS — stops vibration and fake call.
     */
    fun cancelSOS(context: Context) {
        stopFakeCall()
        val vibrator = getVibrator(context)
        vibrator?.cancel()
        Log.d(TAG, "SOS cancelled")
    }
    
    /**
     * Log an SOS event to SharedPreferences.
     */
    private fun logSOSEvent(context: Context, event: SOSEvent) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(SOS_LOG_KEY, mutableSetOf()) ?: mutableSetOf()
        val updated = existing.toMutableSet()
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val entry = "${event.id}|${dateFormat.format(Date(event.timestamp))}|${event.latitude}|${event.longitude}|${event.status}"
        updated.add(entry)
        
        prefs.edit().putStringSet(SOS_LOG_KEY, updated).apply()
    }
    
    /**
     * Retrieve recent SOS log entries.
     */
    fun getRecentSOSLogs(context: Context): List<SOSEvent> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val entries = prefs.getStringSet(SOS_LOG_KEY, emptySet()) ?: emptySet()
        
        return entries.mapNotNull { entry ->
            try {
                val parts = entry.split("|")
                if (parts.size >= 5) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    SOSEvent(
                        id = parts[0],
                        timestamp = dateFormat.parse(parts[1])?.time ?: 0L,
                        latitude = parts[2].toDouble(),
                        longitude = parts[3].toDouble(),
                        status = parts[4]
                    )
                } else null
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse SOS log entry: $entry", e)
                null
            }
        }.sortedByDescending { it.timestamp }
    }
    
    /**
     * Vibrate the SOS Morse pattern.
     */
    private fun vibrateSOSPattern(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(SOS_VIBRATION_PATTERN, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(SOS_VIBRATION_PATTERN, -1)
        }
    }
    
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    
    /**
     * Start fake call simulation — plays default ringtone.
     */
    private fun startFakeCall(context: Context) {
        try {
            stopFakeCall()
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            fakeCallPlayer = MediaPlayer().apply {
                setDataSource(context, ringtoneUri)
                setAudioStreamType(AudioManager.STREAM_RING)
                isLooping = true
                prepare()
                start()
            }
            Log.d(TAG, "Fake call started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start fake call", e)
        }
    }
    
    /**
     * Stop fake call ringtone.
     */
    private fun stopFakeCall() {
        fakeCallPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        fakeCallPlayer = null
    }
}

/**
 * SOS event data model.
 */
data class SOSEvent(
    val id: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val status: String  // ACTIVE, RESOLVED
)
