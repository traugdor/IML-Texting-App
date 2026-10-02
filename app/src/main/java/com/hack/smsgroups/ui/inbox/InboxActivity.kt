package com.hack.smsgroups.ui.inbox

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.hack.smsgroups.data.contacts.ContactResolver
import com.hack.smsgroups.data.provider.TelephonyRepository
import com.hack.smsgroups.data.provider.ThreadSummary
import com.hack.smsgroups.databinding.ActivityInboxBinding
import com.hack.smsgroups.domain.LabelResolver
import com.hack.smsgroups.telephony.DefaultRoleManager
import com.hack.smsgroups.ui.labels.LabelManagerActivity
import com.hack.smsgroups.ui.thread.ThreadActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class InboxActivity : AppCompatActivity() {

    @Inject lateinit var repository: TelephonyRepository
    @Inject lateinit var labelResolver: LabelResolver
    @Inject lateinit var contactResolver: ContactResolver

    private lateinit var binding: ActivityInboxBinding
    private val rows = mutableListOf<ThreadSummary>()

    private val roleRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            renderRoleStatus()
        }

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            loadThreads()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.makeDefault.setOnClickListener {
            roleRequest.launch(DefaultRoleManager.requestRoleIntent(this))
        }
        binding.manageLabels.setOnClickListener {
            startActivity(Intent(this, LabelManagerActivity::class.java))
        }
        binding.threadList.setOnItemClickListener { _, _, position, _ ->
            val row = rows.getOrNull(position) ?: return@setOnItemClickListener
            startActivity(
                Intent(this, ThreadActivity::class.java)
                    .putExtra(ThreadActivity.EXTRA_ADDRESS, row.address)
            )
        }

        ensurePermissions()
    }

    override fun onResume() {
        super.onResume()
        renderRoleStatus()
        if (hasAllPermissions()) loadThreads()
    }

    private fun requiredPermissions(): Array<String> {
        val perms = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms += Manifest.permission.POST_NOTIFICATIONS
        }
        return perms.toTypedArray()
    }

    private fun hasAllPermissions(): Boolean =
        requiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun ensurePermissions() {
        if (!hasAllPermissions()) {
            permissionRequest.launch(requiredPermissions())
        }
    }

    private fun renderRoleStatus() {
        binding.roleStatus.text = if (DefaultRoleManager.isDefaultSmsApp(this)) {
            "Default SMS app: yes"
        } else {
            "Default SMS app: no"
        }
    }

    private fun loadThreads() {
        if (!hasAllPermissions()) return
        lifecycleScope.launch {
            try {
                val loaded = withContext(Dispatchers.IO) { repository.observeThreads() }
                rows.clear()
                rows += loaded
                val labels = loaded.map { row ->
                    val name = labelResolver.labelName(row.address)
                        ?: contactResolver.displayName(row.address)
                        ?: row.address
                    "$name\n${row.snippet ?: ""}"
                }
                binding.threadList.adapter =
                    ArrayAdapter(this@InboxActivity, android.R.layout.simple_list_item_2, android.R.id.text1, labels)
            } catch (e: Exception) {
                binding.roleStatus.text = "Failed to load messages: ${e.javaClass.simpleName}"
            }
        }
    }
}
