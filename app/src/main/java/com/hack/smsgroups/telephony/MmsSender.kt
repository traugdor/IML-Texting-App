package com.hack.smsgroups.telephony

import android.content.Context
import com.hack.smsgroups.domain.Recipients
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MmsSender @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun send(recipients: Recipients, body: String, subId: Int) {
        require(recipients.isGroup) { "MMS send expects a multi-recipient thread" }
        throw NotImplementedError("MMS PDU assembly pending")
    }
}
