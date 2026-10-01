package com.hack.smsgroups.domain

import com.hack.smsgroups.data.db.LabelAddressDao
import com.hack.smsgroups.data.model.LabelAddress
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabelResolver @Inject constructor(
    private val addressDao: LabelAddressDao
) : LabelLookup {

    private val mutex = Mutex()
    private var cache: Map<String, String>? = null

    suspend fun labelName(address: String): String? =
        cacheValue(AddressNormalizer.normalize(address))

    override suspend fun labelNameFor(address: String): String? = labelName(address)

    fun invalidate() {
        cache = null
    }

    suspend fun addressesFor(labelId: Long): List<LabelAddress> =
        addressDao.forLabelOnce(labelId)

    private suspend fun cacheValue(norm: String): String? {
        val snapshot = cache ?: mutex.withLock {
            cache ?: addressDao.allWithNames()
                .associate { it.addressNorm to it.name }
                .also { cache = it }
        }
        return snapshot[norm]
    }
}
