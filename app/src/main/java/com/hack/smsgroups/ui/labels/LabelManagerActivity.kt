package com.hack.smsgroups.ui.labels

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hack.smsgroups.data.db.LabelAddressDao
import com.hack.smsgroups.data.db.LabelDao
import com.hack.smsgroups.data.model.Label
import com.hack.smsgroups.data.model.LabelAddress
import com.hack.smsgroups.databinding.ActivityLabelManagerBinding
import com.hack.smsgroups.domain.AddressNormalizer
import com.hack.smsgroups.domain.LabelResolver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class LabelManagerActivity : AppCompatActivity() {

    @Inject lateinit var labelDao: LabelDao
    @Inject lateinit var addressDao: LabelAddressDao
    @Inject lateinit var resolver: LabelResolver

    private lateinit var binding: ActivityLabelManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabelManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addLabel.setOnClickListener { addFromFields() }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun addFromFields() {
        val name = binding.labelName.text?.toString().orEmpty().trim()
        val number = binding.number.text?.toString().orEmpty().trim()
        if (name.isEmpty()) return

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val labelId = existingLabelId(name) ?: labelDao.insert(Label(name = name))
                if (number.isNotEmpty()) {
                    addressDao.insert(
                        LabelAddress(
                            labelId = labelId,
                            addressRaw = number,
                            addressNorm = AddressNormalizer.normalize(number)
                        )
                    )
                }
            }
            resolver.invalidate()
            binding.labelName.setText("")
            binding.number.setText("")
            load()
        }
    }

    private suspend fun existingLabelId(name: String): Long? {
        val all = labelDao.allOnce()
        return all.firstOrNull { it.name.equals(name, ignoreCase = true) }?.id
    }

    private fun load() {
        lifecycleScope.launch {
            val lines = withContext(Dispatchers.IO) {
                val labels = labelDao.allOnce()
                val out = mutableListOf<String>()
                for (label in labels) {
                    out += label.name
                    for (addr in addressDao.forLabelOnce(label.id)) {
                        out += "    ${addr.addressRaw}"
                    }
                }
                out
            }
            binding.labelList.adapter =
                ArrayAdapter(this@LabelManagerActivity, android.R.layout.simple_list_item_1, lines)
        }
    }
}
