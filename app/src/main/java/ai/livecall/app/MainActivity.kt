package ai.livecall.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat

class MainActivity : Activity() {
    private val prefs by lazy { AppPrefs(this) }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); render() }
    private fun render() {
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(32,40,32,24) }
        root.addView(TextView(this).apply { text="LiveCall AI"; textSize=30f; setTextColor(0xff24233a.toInt()) })
        root.addView(TextView(this).apply { text="Private real-time call companion\n\nThe assistant detects cellular call state and can show a floating panel after you answer. Android generally does not expose the far-end cellular audio to third-party apps, so this build uses speakerphone + microphone recognition where available. It never fabricates transcription."; textSize=16f; setPadding(0,16,0,20) })
        val status = TextView(this).apply { text = if (Settings.canDrawOverlays(this@MainActivity)) "Overlay permission: ready" else "Overlay permission: required"; setPadding(0,8,0,8) }; root.addView(status)
        val endpoint = EditText(this).apply { hint="AI endpoint"; setText(prefs.endpoint) }; root.addView(endpoint)
        val key = EditText(this).apply { hint="API key (stored locally)"; setText(prefs.apiKey); inputType=0x81 }; root.addView(key)
        val model = EditText(this).apply { hint="Model"; setText(prefs.model) }; root.addView(model)
        val lang = EditText(this).apply { hint="Recognition language, e.g. en-US or auto"; setText(prefs.language) }; root.addView(lang)
        val history = CheckBox(this).apply { text="Enable local conversation history (off by default)"; isChecked=prefs.history }; root.addView(history)
        val save = Button(this).apply { text="Save settings"; setOnClickListener { prefs.endpoint=endpoint.text.toString(); prefs.apiKey=key.text.toString(); prefs.model=model.text.toString(); prefs.language=lang.text.toString().ifBlank { "auto" }; prefs.history=history.isChecked; Toast.makeText(this@MainActivity,"Saved",Toast.LENGTH_SHORT).show() } }; root.addView(save)
        val overlay = Button(this).apply { text="Grant floating overlay permission"; setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) } }; root.addView(overlay)
        val notifications = Button(this).apply { text="Enable WhatsApp call popup"; setOnClickListener { startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) } }; root.addView(notifications)
        val mic = Button(this).apply { text="Grant microphone and phone permissions"; setOnClickListener { ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG), 7) } }; root.addView(mic)
        val note = TextView(this).apply { text="Privacy: audio is not saved by this app. Recognized text is sent to your configured AI endpoint only when you provide a key. The Android phone app remains the call UI; LiveCall AI does not answer, speak, or control the caller."; textSize=13f; setPadding(0,24,0,0) }; root.addView(note)
        setContentView(ScrollView(this).apply { addView(root, ViewGroup.LayoutParams(-1,-2)) })
    }
}
