package com.hack.smsgroups.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MmsPushReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        try {
            Log.i(TAG, "MMS push received; PDU decode not yet implemented")
        } finally {
            pending.finish()
        }
    }

    private companion object {
        const val TAG = "MmsPushReceiver"
    }
}
