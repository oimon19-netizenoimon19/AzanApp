
package com.abdulwahhab.azan.alarm
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.abdulwahhab.azan.audio.AthanPlaybackService

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val p = intent.getStringExtra("prayer") ?: return
        AthanPlaybackService.start(context, p)
    }
}
