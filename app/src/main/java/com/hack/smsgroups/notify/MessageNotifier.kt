package com.hack.smsgroups.notify

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import com.hack.smsgroups.R
import com.hack.smsgroups.data.contacts.ContactResolver
import com.hack.smsgroups.domain.LabelResolver
import com.hack.smsgroups.ui.thread.ThreadActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val labelResolver: LabelResolver,
    private val contactResolver: ContactResolver
) {

    suspend fun notifyIncoming(address: String, body: String, timestamp: Long) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        Channels.ensure(context)

        val labelName = labelResolver.labelName(address)
        val displayName = labelName
            ?: contactResolver.displayName(address)
            ?: address

        val intent = Intent(context, ThreadActivity::class.java).apply {
            putExtra(ThreadActivity.EXTRA_ADDRESS, address)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            address.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val self = Person.Builder().setName("Me").build()
        val sender = Person.Builder().setName(displayName).build()
        val style = NotificationCompat.MessagingStyle(self)
            .setConversationTitle(displayName)
            .addMessage(body, timestamp, sender)

        val notification = NotificationCompat.Builder(context, Channels.MESSAGES)
            .setSmallIcon(R.drawable.ic_message)
            .setStyle(style)
            .setContentTitle(displayName)
            .setContentText(body)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        NotificationManagerCompat.from(context).notify(address.hashCode(), notification)
    }
}
