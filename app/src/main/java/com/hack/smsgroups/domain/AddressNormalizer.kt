package com.hack.smsgroups.domain

import android.telephony.PhoneNumberUtils

object AddressNormalizer {

    fun normalize(raw: String): String {
        val trimmed = raw.trim()
        if (isShortOrAlphanumeric(trimmed)) return trimmed.uppercase()
        val digits = buildString {
            for (c in trimmed) {
                if (c.isDigit() || c == '+') append(c)
            }
        }
        val withPlus = if (digits.startsWith("00")) "+" + digits.substring(2) else digits
        return PhoneNumberUtils.normalizeNumber(withPlus)
    }

    fun isShortOrAlphanumeric(raw: String): Boolean {
        val trimmed = raw.trim()
        val hasLetter = trimmed.any { it.isLetter() }
        if (hasLetter) return true
        val digits = trimmed.count { it.isDigit() }
        return trimmed.startsWith("*") || trimmed.startsWith("#") || digits in 3..6
    }

    fun areEquivalent(a: String, b: String): Boolean {
        return normalize(a) == normalize(b)
    }
}
