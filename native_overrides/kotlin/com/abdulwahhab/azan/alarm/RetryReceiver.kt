
package com.abdulwahhab.azan.alarm
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RetryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val attempt = intent.getIntExtra("attempt", 1)
        val ok = try { true } catch (_: Exception) { false }
        if (!ok) RetryManager.scheduleRetry(context, attempt)
    }
}
