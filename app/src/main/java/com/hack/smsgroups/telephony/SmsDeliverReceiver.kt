package com.hack.smsgroups.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.hack.smsgroups.data.provider.TelephonyRepository
import com.hack.smsgroups.notify.MessageNotifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsDeliverReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: TelephonyRepository
    @Inject lateinit var notifier: MessageNotifier

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val pending = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                val bySender = messages.groupBy { it.originatingAddress ?: it.displayOriginatingAddress ?: "" }
                for ((address, parts) in bySender) {
                    if (address.isEmpty()) continue
                    val body = parts.joinToString(separator = "") { it.messageBody ?: "" }
                    val timestamp = parts.first().timestampMillis
                    val subId = parts.first().subscriptionId

                    repository.writeIncoming(address, body, timestamp, subId)
                    notifier.notifyIncoming(address = address, body = body, timestamp = timestamp)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
