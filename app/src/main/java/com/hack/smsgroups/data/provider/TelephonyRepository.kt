package com.hack.smsgroups.data.provider

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ThreadSummary(
    val threadId: Long,
    val address: String,
    val snippet: String?,
    val date: Long
)

data class SmsMessageRow(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val isOutgoing: Boolean
)

@Singleton
class TelephonyRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val smsUri: Uri = Telephony.Sms.CONTENT_URI

    fun observeThreads(): List<ThreadSummary> {
        val out = mutableListOf<ThreadSummary>()
        try {
            context.contentResolver.query(
                smsUri,
                arrayOf(
                    Telephony.Sms.THREAD_ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )?.use { c -> collectThreads(c, out) }
        } catch (e: SecurityException) {
            return emptyList()
        }
        return out
    }

    fun messagesForThread(threadId: Long): List<SmsMessageRow> {
        val out = mutableListOf<SmsMessageRow>()
        try {
            context.contentResolver.query(
                smsUri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.THREAD_ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                ),
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString()),
                "${Telephony.Sms.DATE} ASC"
            )?.use { c -> collectMessages(c, out) }
        } catch (e: SecurityException) {
            return emptyList()
        }
        return out
    }

    fun messagesForAddress(address: String): List<SmsMessageRow> {
        val out = mutableListOf<SmsMessageRow>()
        try {
            context.contentResolver.query(
                smsUri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.THREAD_ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                ),
                "${Telephony.Sms.ADDRESS} = ?",
                arrayOf(address),
                "${Telephony.Sms.DATE} ASC"
            )?.use { c -> collectMessages(c, out) }
        } catch (e: SecurityException) {
            return emptyList()
        }
        return out
    }

    fun writeIncoming(address: String, body: String, timestamp: Long, subscriptionId: Int): Uri? {
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, timestamp)
            put(Telephony.Sms.DATE_SENT, timestamp)
            put(Telephony.Sms.READ, 0)
            put(Telephony.Sms.SEEN, 0)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
            if (subscriptionId >= 0) put(Telephony.Sms.SUBSCRIPTION_ID, subscriptionId)
        }
        return try {
            context.contentResolver.insert(smsUri, values)
        } catch (e: SecurityException) {
            null
        }
    }

    fun writeOutgoing(address: String, body: String, timestamp: Long, subscriptionId: Int): Uri? {
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, timestamp)
            put(Telephony.Sms.READ, 1)
            put(Telephony.Sms.SEEN, 1)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
            if (subscriptionId >= 0) put(Telephony.Sms.SUBSCRIPTION_ID, subscriptionId)
        }
        return try {
            context.contentResolver.insert(smsUri, values)
        } catch (e: SecurityException) {
            null
        }
    }

    private fun collectThreads(c: Cursor, out: MutableList<ThreadSummary>) {
        val iThread = c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
        val iAddress = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
        val iBody = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
        val iDate = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
        while (c.moveToNext()) {
            out += ThreadSummary(
                threadId = c.getLong(iThread),
                address = c.getString(iAddress) ?: "",
                snippet = c.getString(iBody),
                date = c.getLong(iDate)
            )
        }
    }

    private fun collectMessages(c: Cursor, out: MutableList<SmsMessageRow>) {
        val iId = c.getColumnIndexOrThrow(Telephony.Sms._ID)
        val iThread = c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
        val iAddress = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
        val iBody = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
        val iDate = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
        val iType = c.getColumnIndexOrThrow(Telephony.Sms.TYPE)
        while (c.moveToNext()) {
            out += SmsMessageRow(
                id = c.getLong(iId),
                threadId = c.getLong(iThread),
                address = c.getString(iAddress) ?: "",
                body = c.getString(iBody) ?: "",
                date = c.getLong(iDate),
                isOutgoing = c.getInt(iType) == Telephony.Sms.MESSAGE_TYPE_SENT
            )
        }
    }
}
