package com.hack.smsgroups.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MmsSentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val ok = resultCode == android.app.Activity.RESULT_OK
        Toast.makeText(
            context,
            if (ok) "MMS sent" else "MMS failed",
            Toast.LENGTH_SHORT
        ).show()
    }
}
