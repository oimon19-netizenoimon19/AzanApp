
package com.abdulwahhab.azan.alarm
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object RetryManager {
    private const val ACTION = "com.azan.RETRY_SCHEDULE"
    private const val MAX_ATTEMPTS = 4
    private const val GAP_MINUTES = 30
    fun scheduleRetry(context: Context, currentAttempt: Int) {
        if (currentAttempt >= MAX_ATTEMPTS) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + GAP_MINUTES * 60_000L
        val i = Intent(context, RetryReceiver::class.java).apply {
            action = ACTION; putExtra("attempt", currentAttempt + 1)
        }
        val pi = PendingIntent.getBroadcast(context, 9999, i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }
}
