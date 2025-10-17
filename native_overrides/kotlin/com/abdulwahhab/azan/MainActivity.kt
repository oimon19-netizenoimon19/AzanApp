
package com.abdulwahhab.azan

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.abdulwahhab.azan.alarm.AlarmScheduler
import com.abdulwahhab.azan.alarm.Prayer
import com.abdulwahhab.azan.store.SettingsStore
import com.abdulwahhab.azan.audio.AthanPlaybackService

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.abdulwahhab.azan/channel"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "scheduleWithTimes" -> {
                    val times = call.argument<Map<String, Long>>("times") ?: mapOf()
                    val files = call.argument<Map<String, String>>("files") ?: mapOf()
                    val ss = SettingsStore(this)
                    Prayer.values().forEach { p ->
                        files[p.name]?.let { ss.setPath(p, it) }
                    }
                    val ok = try {
                        AlarmScheduler.scheduleWithEpochs(this, times)
                        true
                    } catch (e: Exception) { false }
                    if (!ok) com.abdulwahhab.azan.alarm.RetryManager.scheduleRetry(this, 0)
                    result.success(ok)
                }
                "playNow" -> {
                    val p = call.argument<String>("prayer") ?: ""
                    AthanPlaybackService.start(this, p)
                    result.success(true)
                }
                else -> result.notImplemented()
            }
        }
    }
}
