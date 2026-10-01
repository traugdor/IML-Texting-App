package com.hack.smsgroups.ui.thread

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hack.smsgroups.data.contacts.ContactResolver
import com.hack.smsgroups.data.provider.TelephonyRepository
import com.hack.smsgroups.databinding.ActivityThreadBinding
import com.hack.smsgroups.domain.LabelResolver
import com.hack.smsgroups.domain.Recipients
import com.hack.smsgroups.telephony.DefaultRoleManager
import com.hack.smsgroups.telephony.SmsSender
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ThreadActivity : AppCompatActivity() {

    @Inject lateinit var repository: TelephonyRepository
    @Inject lateinit var labelResolver: LabelResolver
    @Inject lateinit var contactResolver: ContactResolver
    @Inject lateinit var smsSender: SmsSender

    private lateinit var binding: ActivityThreadBinding
    private lateinit var address: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThreadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        address = intent.getStringExtra(EXTRA_ADDRESS).orEmpty()

        binding.send.setOnClickListener {
            val body = binding.composer.text?.toString().orEmpty().trim()
            if (body.isEmpty() || address.isEmpty()) return@setOnClickListener
            if (!DefaultRoleManager.isDefaultSmsApp(this)) return@setOnClickListener
            val recipients = Recipients.of(address)
            smsSender.send(recipients, body, subId = -1)
            binding.composer.setText("")
            load()
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            val labelName = labelResolver.labelName(address)
                ?: contactResolver.displayName(address)
                ?: address
            binding.threadTitle.text = labelName
            val messages = withContext(Dispatchers.IO) { repository.messagesForAddress(address) }
            val lines = messages.map { msg ->
                (if (msg.isOutgoing) "Me: " else "") + msg.body
            }
            binding.messageList.adapter =
                ArrayAdapter(this@ThreadActivity, android.R.layout.simple_list_item_1, lines)
        }
    }

    companion object {
        const val EXTRA_ADDRESS = "extra_address"
    }
}
