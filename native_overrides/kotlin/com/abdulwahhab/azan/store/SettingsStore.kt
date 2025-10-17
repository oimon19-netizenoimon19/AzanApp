
package com.abdulwahhab.azan.store
import android.content.Context
import com.abdulwahhab.azan.alarm.Prayer

class SettingsStore(private val ctx: Context) {
    private val sp = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
    fun setPath(prayer: Prayer, path: String) { sp.edit().putString("path_${'$'}{prayer.name}", path).apply() }
    fun pathFor(prayer: Prayer): String? = sp.getString("path_${'$'}{prayer.name}", null)
}
