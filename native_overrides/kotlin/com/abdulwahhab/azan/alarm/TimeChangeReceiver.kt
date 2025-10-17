
package com.abdulwahhab.azan.alarm
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // In a full app you'd re-calc & reschedule here; Flutter will usually re-open daily.
        RetryManager.scheduleRetry(context, 0)
    }
}
