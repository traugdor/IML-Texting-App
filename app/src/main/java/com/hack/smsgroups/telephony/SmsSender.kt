package com.hack.smsgroups.telephony

import android.content.Context
import android.telephony.SmsManager
import com.hack.smsgroups.data.provider.TelephonyRepository
import com.hack.smsgroups.domain.Recipients
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsSender @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TelephonyRepository
) {

    fun send(recipients: Recipients, body: String, subId: Int) {
        val manager = smsManagerFor(subId)
        for (address in recipients.addresses) {
            val parts = manager.divideMessage(body)
            manager.sendMultipartTextMessage(address, null, parts, null, null)
            repository.writeOutgoing(address, body, System.currentTimeMillis(), subId)
        }
    }

    private fun smsManagerFor(subId: Int): SmsManager =
        if (subId >= 0) {
            SmsManager.getSmsManagerForSubscriptionId(subId)
        } else {
            SmsManager.getDefault()
        }
}
