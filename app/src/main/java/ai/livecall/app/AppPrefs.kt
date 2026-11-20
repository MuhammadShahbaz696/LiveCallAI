package ai.livecall.app

import android.content.Context

class AppPrefs(context: Context) {
    private val p = context.getSharedPreferences("livecall", Context.MODE_PRIVATE)
    var endpoint: String get() = p.getString("endpoint", "https://api.openai.com/v1/chat/completions") ?: ""; set(v) = p.edit().putString("endpoint", v).apply()
    var apiKey: String get() = p.getString("key", "") ?: ""; set(v) = p.edit().putString("key", v).apply()
    var model: String get() = p.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini"; set(v) = p.edit().putString("model", v).apply()
    var history: Boolean get() = p.getBoolean("history", false); set(v) = p.edit().putBoolean("history", v).apply()
    var language: String get() = p.getString("language", "auto") ?: "auto"; set(v) = p.edit().putString("language", v).apply()
}
