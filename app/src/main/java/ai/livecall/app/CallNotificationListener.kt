package ai.livecall.app

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat

/** Shows the same assistant entry point for WhatsApp call notifications.
 * Android does not expose WhatsApp's protected call audio to this service. */
class CallNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName.lowercase()
        if (!pkg.contains("whatsapp")) return
        val n: Notification = sbn.notification
        val text = n.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.lowercase() ?: ""
        val title = n.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "WhatsApp call"
        if (listOf("call", "calling", "incoming", "video").none { text.contains(it) || title.lowercase().contains(it) }) return
        val i = Intent(this, CallAssistantService::class.java).putExtra("state", "WHATSAPP_RINGING").putExtra("source", "WhatsApp").putExtra("caller", title)
        ContextCompat.startForegroundService(this, i)
    }
}
