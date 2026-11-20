package ai.livecall.app

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiResult(val meaning: String, val reply: String)

class AiClient(private val prefs: AppPrefs) {
    private val http = OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build()
    fun generate(transcript: String, context: String): AiResult {
        require(prefs.apiKey.isNotBlank()) { "Add an API key in Settings" }
        val system = """You are my private real-time phone call assistant. The call can be about absolutely any subject. Never speak to the caller. For the caller message below, return ONLY JSON with keys meaning_roman_urdu and suggested_reply. Use easy conversational Roman Urdu for meaning and short natural spoken English for reply. Never invent personal facts. If unclear, suggest asking the caller to repeat. Use the recent context when relevant."""
        val messages = JSONArray().put(JSONObject().put("role", "system").put("content", system)).put(JSONObject().put("role", "user").put("content", "Recent context:\n$context\n\nCaller message:\n$transcript"))
        val body = JSONObject().put("model", prefs.model).put("temperature", 0.2).put("messages", messages).toString().toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url(prefs.endpoint).header("Authorization", "Bearer ${prefs.apiKey}").post(body).build()
        http.newCall(req).execute().use { r ->
            if (!r.isSuccessful) error("AI request failed: HTTP ${r.code}")
            val text = JSONObject(r.body?.string() ?: "").getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            val json = JSONObject(text.substringAfter("{").substringBeforeLast("}").let { "{$it}" })
            return AiResult(json.optString("meaning_roman_urdu", "Samajh nahi aya; caller se dobara poochain."), json.optString("suggested_reply", "Could you please repeat that?"))
        }
    }
}
