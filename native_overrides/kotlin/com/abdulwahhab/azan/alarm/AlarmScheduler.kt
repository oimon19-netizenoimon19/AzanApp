
package com.abdulwahhab.azan.alarm
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object AlarmScheduler {
    fun scheduleWithEpochs(context: Context, times: Map<String, Long>) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        fun scheduleOne(prayer: Prayer, epoch: Long?) {
            if (epoch == null) return
            if (epoch <= System.currentTimeMillis()) return
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = "com.azan.PLAY_ADHAN"; putExtra("prayer", prayer.name)
            }
            val pi = PendingIntent.getBroadcast(context, prayer.ordinal, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epoch, pi)
        }
        scheduleOne(Prayer.FAJR, times["fajr"])
        scheduleOne(Prayer.DHUHR, times["dhuhr"])
        scheduleOne(Prayer.ASR, times["asr"])
        scheduleOne(Prayer.MAGHRIB, times["maghrib"])
        scheduleOne(Prayer.ISHA, times["isha"])
    }
}
