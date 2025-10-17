
package com.abdulwahhab.azan.audio
import android.app.*
import android.content.*
import android.media.*
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.abdulwahhab.azan.R
import com.abdulwahhab.azan.alarm.Prayer
import com.abdulwahhab.azan.store.SettingsStore
import java.io.File

class AthanPlaybackService : Service() {
    companion object {
        private const val CH_ID = "playback"
        fun start(ctx: Context, prayer: String) {
            val i = Intent(ctx, AthanPlaybackService::class.java).putExtra("prayer", prayer)
            ctx.startForegroundService(i)
        }
    }
    private var player: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private lateinit var afr: AudioFocusRequest

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val channel = NotificationChannel(CH_ID, "Playback", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayer = intent?.getStringExtra("prayer") ?: return START_NOT_STICKY
        startForeground(1, notif("Playing Adhān (${prayer.lowercase().replaceFirstChar { it.uppercase() }})"))
        val path = SettingsStore(this).pathFor(Prayer.valueOf(prayer)) ?: return stopSelf().let { START_NOT_STICKY }
        val uri = if (path.startsWith("content://")) Uri.parse(path) else Uri.fromFile(File(path))
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        afr = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).setAudioAttributes(attrs).setOnAudioFocusChangeListener { }.build()
        if (audioManager.requestAudioFocus(afr) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { stopSelf(); return START_NOT_STICKY }
        player = MediaPlayer().apply {
            setAudioAttributes(attrs)
            setDataSource(applicationContext, uri)
            setOnCompletionListener { cleanupStop() }
            setOnPreparedListener { start() }
            setOnErrorListener { _,_,_ -> cleanupStop(); true }
            prepareAsync()
        }
        return START_STICKY
    }

    private fun notif(content: String) = NotificationCompat.Builder(this, CH_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Azan")
        .setContentText(content).build()

    private fun cleanupStop() {
        try { player?.release() } catch (_: Throwable) {}
        player = null
        try { audioManager.abandonAudioFocusRequest(afr) } catch (_: Throwable) {}
        stopForeground(STOP_FOREGROUND_REMOVE); stopSelf()
    }
    override fun onDestroy() { cleanupStop(); super.onDestroy() }
    override fun onBind(intent: Intent?) = null
}
