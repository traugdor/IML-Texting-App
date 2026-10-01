package com.hack.smsgroups.telephony

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.hack.smsgroups.domain.Recipients
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HeadlessSmsSendService : Service() {

    @Inject lateinit var smsSender: SmsSender

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val recipient = intent?.data?.schemeSpecificPart?.let { Recipients.of(it) }
        val body = intent?.getStringExtra(Intent.EXTRA_TEXT)
        if (recipient != null && body != null) {
            smsSender.send(recipient, body, subId = -1)
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
