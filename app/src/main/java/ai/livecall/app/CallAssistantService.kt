package ai.livecall.app

import android.app.*
import android.content.*
import android.media.AudioManager
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import java.util.Locale
import java.util.concurrent.Executors

class CallAssistantService : Service() {
    private val main = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var panel: View? = null
    private val executor = Executors.newSingleThreadExecutor()
    private val prefs by lazy { AppPrefs(this) }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel(); startForeground(10, notification("Waiting for the call audio source…"))
        if (intent?.getStringExtra("state") == android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK || intent?.getStringExtra("state") == "WHATSAPP_RINGING") startAssistant()
        return START_NOT_STICKY
    }
    private fun startAssistant() {
        if (panel != null) return
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        am.isSpeakerphoneOn = true
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28,20,28,20); setBackgroundColor(0xff181923.toInt()) }
        val title = TextView(this).apply { text = "LIVE CALL AI  •  LISTENING"; setTextColor(0xffb9b2ff.toInt()); textSize = 12f }
        val caller = TextView(this).apply { text = "Caller message will appear here"; setTextColor(-1); textSize = 18f; setPadding(0,18,0,10) }
        val meaning = TextView(this).apply { text = "Roman Urdu: waiting for speech…"; setTextColor(0xffd7d5df.toInt()); textSize = 16f }
        val reply = TextView(this).apply { text = "Suggested reply: —"; setTextColor(0xffa8e6cf.toInt()); textSize = 16f; setPadding(0,10,0,12) }
        box.addView(title); box.addView(caller); box.addView(meaning); box.addView(reply)
        box.setOnTouchListener(DragTouch(wm, box))
        val params = WindowManager.LayoutParams(-1, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, -3).apply { gravity = Gravity.TOP; y = 90 }
        if (!android.provider.Settings.canDrawOverlays(this)) return
        wm.addView(box, params); panel = box
        beginRecognition(caller, meaning, reply)
    }
    private fun beginRecognition(caller: TextView, meaning: TextView, reply: TextView) {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { caller.text = "Audio unavailable: speech recognition is not available on this device"; return }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).also { sr ->
            sr.setRecognitionListener(object : RecognitionListener {
                override fun onResults(b: android.os.Bundle) { val text = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: return; caller.text = text; executor.execute { try { val r = AiClient(prefs).generate(text, ""); main.post { meaning.text = "Roman Urdu: ${r.meaning}"; reply.text = "Suggested reply: ${r.reply}" } } catch(e: Exception) { main.post { meaning.text = "AI unavailable: ${e.message}" } } }; sr.startListening(intent()) }
                override fun onError(error: Int) { caller.text = "Listening unavailable (code $error). Check microphone/audio routing."; sr.startListening(intent()) }
                override fun onReadyForSpeech(p: android.os.Bundle?) {}; override fun onBeginningOfSpeech() {}; override fun onRmsChanged(r: Float) {}; override fun onBufferReceived(b: ByteArray?) {}; override fun onEndOfSpeech() {}; override fun onPartialResults(b: android.os.Bundle?) {}; override fun onEvent(t: Int, p: android.os.Bundle?) {}
            }); sr.startListening(intent())
        }
    }
    private fun intent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (prefs.language == "auto") Locale.getDefault() else prefs.language); putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) }
    override fun onDestroy() { recognizer?.destroy(); panel?.let { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) }; executor.shutdown(); super.onDestroy() }
    private fun createChannel() { val nm=getSystemService(NOTIFICATION_SERVICE) as NotificationManager; nm.createNotificationChannel(NotificationChannel("call", "LiveCall AI", NotificationManager.IMPORTANCE_LOW)) }
    private fun notification(text:String)=NotificationCompat.Builder(this,"call").setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentTitle("LiveCall AI").setContentText(text).setOngoing(true).build()
    private class DragTouch(val wm:WindowManager,val view:View): View.OnTouchListener { var down=0f; var base=0; override fun onTouch(v:View,e:android.view.MotionEvent):Boolean { if(e.action==0){down=e.rawY;base=(view.layoutParams as WindowManager.LayoutParams).y;return true}; if(e.action==2){val p=view.layoutParams as WindowManager.LayoutParams;p.y=base+(e.rawY-down).toInt();wm.updateViewLayout(view,p);return true};return true } }
}
