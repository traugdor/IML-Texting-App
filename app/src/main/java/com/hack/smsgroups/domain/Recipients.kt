package com.hack.smsgroups.domain

@JvmInline
value class Recipients private constructor(val addresses: List<String>) {

    val isGroup: Boolean get() = addresses.size > 1

    companion object {
        fun of(vararg addresses: String): Recipients =
            Recipients(addresses.map { it.trim() }.filter { it.isNotEmpty() }.toList())

        fun of(addresses: Collection<String>): Recipients =
            Recipients(addresses.map { it.trim() }.filter { it.isNotEmpty() }.toList())
    }
}

interface LabelLookup {
    suspend fun labelNameFor(address: String): String?
}
