package ai.livecall.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

class CallStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val service = Intent(context, CallAssistantService::class.java).putExtra("state", state)
        if (state == TelephonyManager.EXTRA_STATE_IDLE) context.stopService(service)
        else ContextCompat.startForegroundService(context, service)
    }
}
